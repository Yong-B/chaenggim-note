package org.example.chaenggim.domain.visit.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.chaenggim.domain.commitment.entity.Commitment;
import org.example.chaenggim.domain.commitment.repository.CommitmentRepository;
import org.example.chaenggim.domain.commitment.repository.VisitCommitmentCount;
import org.example.chaenggim.domain.doctor.dto.DoctorRef;
import org.example.chaenggim.domain.doctor.entity.Doctor;
import org.example.chaenggim.domain.doctor.repository.DoctorRepository;
import org.example.chaenggim.domain.question.entity.Question;
import org.example.chaenggim.domain.question.entity.QuestionStatus;
import org.example.chaenggim.domain.question.repository.QuestionRepository;
import org.example.chaenggim.domain.question.repository.VisitQuestionCount;
import org.example.chaenggim.domain.visit.dto.CommitmentItem;
import org.example.chaenggim.domain.visit.dto.QuestionItem;
import org.example.chaenggim.domain.visit.dto.VisitCreateRequest;
import org.example.chaenggim.domain.visit.dto.VisitResponse;
import org.example.chaenggim.domain.visit.dto.VisitUpdateRequest;
import org.example.chaenggim.domain.visit.entity.Visit;
import org.example.chaenggim.domain.visit.repository.VisitRepository;
import org.example.chaenggim.global.error.BusinessException;
import org.example.chaenggim.global.error.ErrorCode;
import org.example.chaenggim.global.error.FieldErrorItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitService {

    static final String VISIT_DATE_FUTURE = "방문 날짜는 오늘 이후일 수 없습니다.";
    static final String DUE_DATE_PAST = "약속 기한은 오늘 이전일 수 없습니다.";

    private final Clock clock;
    private final DoctorRepository doctorRepository;
    private final VisitRepository visitRepository;
    private final QuestionRepository questionRepository;
    private final CommitmentRepository commitmentRepository;

    /**
     * 한 트랜잭션: 의료진 조회(숨김이면 404) → 방문 날짜 미래 검사 → 약속 기한 과거 검사 → 방문·질문·약속 저장
     * → 다음 방문 예정일 반영(null이면 유지, D13). 서비스 검사 에러는 모아서 한 번에 던진다.
     */
    @Transactional
    public VisitResponse create(Long doctorId, VisitCreateRequest request) {
        Doctor doctor = doctorRepository.findActiveById(doctorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        LocalDate today = LocalDate.now(clock);

        List<FieldErrorItem> errors = new ArrayList<>();
        if (request.visitDate().isAfter(today)) {
            errors.add(new FieldErrorItem("visitDate", VISIT_DATE_FUTURE));
        }
        List<CommitmentItem> commitmentItems = request.commitments();
        for (int i = 0; i < commitmentItems.size(); i++) {
            if (commitmentItems.get(i).dueDate().isBefore(today)) {
                errors.add(new FieldErrorItem("commitments[" + i + "].dueDate", DUE_DATE_PAST));
            }
        }
        if (!errors.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, errors);
        }

        Visit visit = visitRepository.save(Visit.builder()
                .doctor(doctor)
                .visitDate(request.visitDate())
                .memo(request.memo())
                .build());
        for (QuestionItem item : request.questions()) {
            questionRepository.save(Question.builder().visit(visit).content(item.content()).build());
        }
        for (CommitmentItem item : commitmentItems) {
            commitmentRepository.save(Commitment.builder()
                    .visit(visit).content(item.content()).dueDate(item.dueDate()).build());
        }
        doctor.changeNextVisitDate(request.nextVisitDate());

        return VisitResponse.of(visit, request.questions().size(), commitmentItems.size(), false);
    }

    /** GET /api/visits/{id}: VisitResponse + doctor(DoctorRef). */
    public VisitResponse get(Long id) {
        Visit visit = findActive(id);
        return toResponse(visit).withDoctor(DoctorRef.from(visit.getDoctor()));
    }

    /** D15: 날짜·메모만. 전달 완료 질문이 있어도 날짜 수정은 허용(R2). */
    @Transactional
    public VisitResponse update(Long id, VisitUpdateRequest request) {
        Visit visit = findActive(id);
        if (request.visitDate().isAfter(LocalDate.now(clock))) {
            throw BusinessException.fieldError("visitDate", VISIT_DATE_FUTURE);
        }
        visit.update(request.visitDate(), request.memo());
        return toResponse(visit);
    }

    /** D16: 전달 완료 질문이 있으면 삭제 불가. 아니면 질문 → 약속 → 방문 순서로 삭제. */
    @Transactional
    public void delete(Long id) {
        Visit visit = findActive(id);
        if (questionRepository.existsByVisit_IdAndStatus(visit.getId(), QuestionStatus.DELIVERED)) {
            throw new BusinessException(ErrorCode.VISIT_HAS_DELIVERED);
        }
        questionRepository.deleteByVisitId(visit.getId());
        commitmentRepository.deleteByVisitId(visit.getId());
        visitRepository.deleteById(visit.getId());
    }

    /** 방문 목록 → 응답. 질문·약속 개수는 GROUP BY 집계 2번으로 가져온다(N+1 없음). */
    public List<VisitResponse> toResponses(List<Visit> visits) {
        if (visits.isEmpty()) {
            return List.of();
        }
        List<Long> visitIds = visits.stream().map(Visit::getId).toList();
        Map<Long, VisitQuestionCount> questionCounts = questionRepository.countByVisitIds(visitIds).stream()
                .collect(Collectors.toMap(VisitQuestionCount::visitId, Function.identity()));
        Map<Long, Long> commitmentCounts = commitmentRepository.countByVisitIds(visitIds).stream()
                .collect(Collectors.toMap(VisitCommitmentCount::visitId, VisitCommitmentCount::count));

        return visits.stream()
                .map(visit -> {
                    VisitQuestionCount q = questionCounts.get(visit.getId());
                    long questionCount = q == null ? 0 : q.questionCount();
                    boolean hasDelivered = q != null && q.deliveredCount() > 0;
                    return VisitResponse.of(visit, questionCount,
                            commitmentCounts.getOrDefault(visit.getId(), 0L), hasDelivered);
                })
                .toList();
    }

    private VisitResponse toResponse(Visit visit) {
        return toResponses(List.of(visit)).get(0);
    }

    private Visit findActive(Long id) {
        return visitRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }
}
