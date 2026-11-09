package org.example.chaenggim.domain.seed.service;

import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.example.chaenggim.domain.commitment.entity.Commitment;
import org.example.chaenggim.domain.commitment.repository.CommitmentRepository;
import org.example.chaenggim.domain.doctor.entity.Doctor;
import org.example.chaenggim.domain.doctor.repository.DoctorRepository;
import org.example.chaenggim.domain.question.entity.Question;
import org.example.chaenggim.domain.question.entity.SourceType;
import org.example.chaenggim.domain.question.repository.QuestionRepository;
import org.example.chaenggim.domain.visit.entity.Visit;
import org.example.chaenggim.domain.visit.repository.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 가상 시드 데이터 (docs/08_시드데이터.md). 모든 이름·병원은 가상이다.
 * 엔티티 빌더와 도메인 메서드만 사용한다(요청 검증을 거치지 않으므로 지난 기한 약속도 만들 수 있다).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeedService {

    private final Clock clock;
    private final DoctorRepository doctorRepository;
    private final VisitRepository visitRepository;
    private final QuestionRepository questionRepository;
    private final CommitmentRepository commitmentRepository;

    /** 전체 삭제(question → commitment → visit → doctor) 후 seed(). 한 트랜잭션. */
    @Transactional
    public void reset() {
        questionRepository.deleteAllInBatch();
        commitmentRepository.deleteAllInBatch();
        visitRepository.deleteAllInBatch();
        doctorRepository.deleteAllInBatch();
        seed();
    }

    /** 저장 순서: 의료진 → 방문 → 질문 → 약속. */
    @Transactional
    public void seed() {
        LocalDate t = LocalDate.now(clock);

        // 1. 의료진
        Doctor d1 = doctorRepository.save(Doctor.builder()
                .name("김가상").hospital("가상내과의원").department("내과")
                .memo("오전 진료 끝난 뒤 11시 30분쯤 방문 선호").nextVisitDate(t.plusDays(1))
                .build());
        Doctor d2 = doctorRepository.save(Doctor.builder()
                .name("이예시").hospital("예시가정의학과의원").department("가정의학과")
                .nextVisitDate(t.plusDays(3))
                .build());
        Doctor d3 = doctorRepository.save(Doctor.builder()
                .name("박샘플").hospital("샘플대학병원").department("순환기내과")
                .memo("외래 대기가 길어 짧게 대화").nextVisitDate(t.minusDays(2))
                .build());
        doctorRepository.save(Doctor.builder()
                .name("최데모").hospital("데모종합병원").department("종양내과")
                .build());

        // 2. 방문
        Visit v1 = visitRepository.save(Visit.builder()
                .doctor(d1).visitDate(t.minusDays(21)).memo("첫 방문. 인사 및 명함 전달").build());
        Visit v2 = visitRepository.save(Visit.builder()
                .doctor(d1).visitDate(t.minusDays(7)).memo("고지혈증 환자 복합제 처방 경험 이야기 나눔").build());
        Visit v3 = visitRepository.save(Visit.builder()
                .doctor(d2).visitDate(t.minusDays(5)).memo("고혈압 환자 복약 편의성에 관심").build());
        Visit v4 = visitRepository.save(Visit.builder()
                .doctor(d3).visitDate(t.minusDays(10)).memo("외래 대기가 길어 5분만 대화").build());

        // 3. 질문 (답변 문구는 효능·안전성 주장 없이 "확인했다 / 전달 예정" 수준. 바꾸지 않는다)
        Question q1 = question(v2, "고령 환자 관련 로수젯 자료가 있는지 궁금하다고 하심");
        q1.answer("학술팀에 확인한 자료 목록을 정리함. 요약본을 다음 방문 때 전달 예정.",
                SourceType.MEDICAL_TEAM, "학술팀 메일 회신", t.minusDays(3).atTime(16, 0));

        Question q2 = question(v2, "아모잘탄 용량별 제형 차이를 물어보심");
        q2.startChecking();

        Question q3 = question(v3, "3제 복합 고혈압약은 어떤 경우에 먼저 고려하는지 물어보심");

        Question q4 = question(v3, "환자용 복약 안내 자료가 있는지 물어보심");
        q4.answer("환자용 안내 자료 제공 가능 여부를 학술팀에 확인함. 제공 가능한 자료 목록 전달 예정.",
                SourceType.MEDICAL_TEAM, null, t.minusDays(1).atTime(11, 0));

        Question q5 = question(v4, "에페 출시 일정이 정해졌는지 물어보심");
        q5.answer("공식 안내된 일정 범위 안에서만 전달하기로 하고, 회사 공지 내용을 확인함.",
                SourceType.LABEL, "회사 공지", t.minusDays(8).atTime(10, 0));
        q5.deliver(t.minusDays(6).atTime(14, 0));

        questionRepository.save(q1);
        questionRepository.save(q2);
        questionRepository.save(q3);
        questionRepository.save(q4);
        questionRepository.save(q5);

        // 4. 약속
        Commitment c1 = commitment(v1, "학술 행사 일정 공유", t.minusDays(20));
        c1.complete(t.minusDays(20).atTime(18, 0));
        commitmentRepository.save(c1);
        commitmentRepository.save(commitment(v2, "고령 환자 자료 요약본 전달", t.minusDays(2)));
        commitmentRepository.save(commitment(v3, "환자용 안내 자료 샘플 전달", t));
        commitmentRepository.save(commitment(v4, "학술 행사 안내문 전달", t.plusDays(4)));
    }

    private static Question question(Visit visit, String content) {
        return Question.builder().visit(visit).content(content).build();
    }

    private static Commitment commitment(Visit visit, String content, LocalDate dueDate) {
        return Commitment.builder().visit(visit).content(content).dueDate(dueDate).build();
    }
}
