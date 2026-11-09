package org.example.chaenggim.domain.seed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import org.example.chaenggim.domain.commitment.entity.Commitment;
import org.example.chaenggim.domain.commitment.repository.CommitmentRepository;
import org.example.chaenggim.domain.doctor.entity.Doctor;
import org.example.chaenggim.domain.doctor.repository.DoctorRepository;
import org.example.chaenggim.domain.question.entity.Question;
import org.example.chaenggim.domain.question.entity.QuestionStatus;
import org.example.chaenggim.domain.question.repository.QuestionRepository;
import org.example.chaenggim.domain.visit.repository.VisitRepository;
import org.example.chaenggim.global.config.TestClockConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Import(TestClockConfig.class)
class SeedServiceTest {

    private static final LocalDate T = LocalDate.of(2026, 11, 9);

    @Autowired SeedService seedService;
    @Autowired DoctorRepository doctorRepository;
    @Autowired VisitRepository visitRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired CommitmentRepository commitmentRepository;

    private void assertCounts() {
        assertThat(doctorRepository.count()).isEqualTo(4);
        assertThat(visitRepository.count()).isEqualTo(4);
        assertThat(questionRepository.count()).isEqualTo(5);
        assertThat(commitmentRepository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("S-15 reset을 두 번 해도 매번 의료진 4·방문 4·질문 5·약속 4")
    void resetTwice() {
        seedService.reset();
        assertCounts();

        seedService.reset();
        assertCounts();
    }

    @Test
    @DisplayName("SeedRunner: 비어 있으면 시드를 넣고, 다시 실행해도 중복되지 않는다")
    void runnerSeedsOnlyWhenEmpty() {
        SeedRunner runner = new SeedRunner(seedService, doctorRepository, true);

        runner.run(null);
        assertCounts();

        runner.run(null);
        assertCounts();
    }

    @Test
    @DisplayName("SeedRunner: seed-on-empty=false면 넣지 않는다")
    void runnerDisabled() {
        new SeedRunner(seedService, doctorRepository, false).run(null);

        assertThat(doctorRepository.count()).isZero();
    }

    @Test
    @DisplayName("시드 내용이 08 문서와 일치한다 (Clock 기준 상대 날짜)")
    void seedContent() {
        seedService.seed();

        assertThat(doctorRepository.searchActive(null))
                .extracting(Doctor::getName, Doctor::getHospital, Doctor::getDepartment, Doctor::getNextVisitDate)
                .containsExactly(
                        tuple("김가상", "가상내과의원", "내과", T.plusDays(1)),
                        tuple("박샘플", "샘플대학병원", "순환기내과", T.minusDays(2)),
                        tuple("이예시", "예시가정의학과의원", "가정의학과", T.plusDays(3)),
                        tuple("최데모", "데모종합병원", "종양내과", null));

        assertThat(questionRepository.findAll())
                .extracting(Question::getContent, Question::getStatus)
                .containsExactlyInAnyOrder(
                        tuple("고령 환자 관련 로수젯 자료가 있는지 궁금하다고 하심", QuestionStatus.READY),
                        tuple("아모잘탄 용량별 제형 차이를 물어보심", QuestionStatus.CHECKING),
                        tuple("3제 복합 고혈압약은 어떤 경우에 먼저 고려하는지 물어보심", QuestionStatus.UNANSWERED),
                        tuple("환자용 복약 안내 자료가 있는지 물어보심", QuestionStatus.READY),
                        tuple("에페 출시 일정이 정해졌는지 물어보심", QuestionStatus.DELIVERED));

        Question q5 = questionRepository.findActiveDelivered(null).get(0);
        assertThat(q5.getAnsweredAt()).isEqualTo(T.minusDays(8).atTime(10, 0));
        assertThat(q5.getDeliveredAt()).isEqualTo(T.minusDays(6).atTime(14, 0));

        assertThat(commitmentRepository.findAll())
                .extracting(Commitment::getContent, Commitment::getDueDate, Commitment::isDone)
                .containsExactlyInAnyOrder(
                        tuple("학술 행사 일정 공유", T.minusDays(20), true),
                        tuple("고령 환자 자료 요약본 전달", T.minusDays(2), false),
                        tuple("환자용 안내 자료 샘플 전달", T, false),
                        tuple("학술 행사 안내문 전달", T.plusDays(4), false));
    }
}
