package org.example.chaenggim.domain.doctor.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.chaenggim.domain.commitment.dto.CommitmentResponse;
import org.example.chaenggim.domain.commitment.repository.CommitmentRepository;
import org.example.chaenggim.domain.doctor.dto.DoctorDetailResponse;
import org.example.chaenggim.domain.doctor.dto.DoctorRequest;
import org.example.chaenggim.domain.doctor.dto.DoctorResponse;
import org.example.chaenggim.domain.doctor.dto.DoctorSummaryResponse;
import org.example.chaenggim.domain.doctor.entity.Doctor;
import org.example.chaenggim.domain.doctor.repository.DoctorRepository;
import org.example.chaenggim.domain.question.dto.QuestionResponse;
import org.example.chaenggim.domain.question.repository.QuestionRepository;
import org.example.chaenggim.domain.visit.dto.VisitResponse;
import org.example.chaenggim.domain.visit.repository.VisitRepository;
import org.example.chaenggim.domain.visit.service.VisitService;
import org.example.chaenggim.global.error.BusinessException;
import org.example.chaenggim.global.error.ErrorCode;
import org.example.chaenggim.global.util.Texts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorService {

    private final Clock clock;
    private final DoctorRepository doctorRepository;
    private final VisitRepository visitRepository;
    private final QuestionRepository questionRepository;
    private final CommitmentRepository commitmentRepository;
    private final VisitService visitService;

    /** 7.4 검색. q가 비어 있으면 전체. */
    public List<DoctorSummaryResponse> search(String q) {
        return doctorRepository.searchActive(Texts.stripToNull(q)).stream()
                .map(DoctorSummaryResponse::from)
                .toList();
    }

    @Transactional
    public DoctorResponse create(DoctorRequest request) {
        Doctor doctor = doctorRepository.save(request.toEntity());
        return DoctorResponse.from(doctor);
    }

    /** 7.2 의료진 상세 + 방문 전 준비. */
    public DoctorDetailResponse get(Long id) {
        Doctor doctor = findActive(id);
        LocalDate today = LocalDate.now(clock);

        List<VisitResponse> visits = visitService.toResponses(visitRepository.findActiveByDoctorId(id));
        DoctorDetailResponse.Prep prep = new DoctorDetailResponse.Prep(
                questionRepository.findReadyByDoctorId(id).stream().map(QuestionResponse::from).toList(),
                questionRepository.findOpenByDoctorId(id).stream().map(QuestionResponse::from).toList(),
                commitmentRepository.findOpenByDoctorId(id).stream()
                        .map(c -> CommitmentResponse.from(c, today)).toList(),
                visits.isEmpty() ? null : visits.get(0));

        return new DoctorDetailResponse(
                DoctorResponse.from(doctor),
                today,
                prep,
                visits,
                questionRepository.findDeliveredByDoctorId(id).stream().map(QuestionResponse::from).toList(),
                commitmentRepository.findDoneByDoctorId(id).stream()
                        .map(c -> CommitmentResponse.from(c, today)).toList());
    }

    /** 수정 화면은 값을 그대로 보내므로 nextVisitDate가 null이면 예정일을 지운다. */
    @Transactional
    public DoctorResponse update(Long id, DoctorRequest request) {
        Doctor doctor = findActive(id);
        doctor.update(request.name(), request.hospital(), request.department(), request.memo(),
                request.nextVisitDate());
        return DoctorResponse.from(doctor);
    }

    /** D14: 숨김 처리(기록 보존). */
    @Transactional
    public void delete(Long id) {
        findActive(id).delete(LocalDateTime.now(clock));
    }

    private Doctor findActive(Long id) {
        return doctorRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }
}
