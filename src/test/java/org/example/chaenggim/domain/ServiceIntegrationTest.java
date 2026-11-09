package org.example.chaenggim.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.util.List;
import org.example.chaenggim.domain.commitment.dto.CommitmentResponse;
import org.example.chaenggim.domain.commitment.dto.CommitmentUpdateRequest;
import org.example.chaenggim.domain.commitment.entity.Commitment;
import org.example.chaenggim.domain.commitment.repository.CommitmentRepository;
import org.example.chaenggim.domain.commitment.service.CommitmentService;
import org.example.chaenggim.domain.doctor.dto.DoctorDetailResponse;
import org.example.chaenggim.domain.doctor.dto.DoctorSummaryResponse;
import org.example.chaenggim.domain.doctor.entity.Doctor;
import org.example.chaenggim.domain.doctor.repository.DoctorRepository;
import org.example.chaenggim.domain.doctor.service.DoctorService;
import org.example.chaenggim.domain.home.dto.HomeResponse;
import org.example.chaenggim.domain.home.service.HomeService;
import org.example.chaenggim.domain.question.dto.AnswerRequest;
import org.example.chaenggim.domain.question.dto.QuestionResponse;
import org.example.chaenggim.domain.question.entity.ProductTag;
import org.example.chaenggim.domain.question.entity.Question;
import org.example.chaenggim.domain.question.entity.QuestionStatus;
import org.example.chaenggim.domain.question.entity.SourceType;
import org.example.chaenggim.domain.question.repository.QuestionRepository;
import org.example.chaenggim.domain.question.service.QuestionService;
import org.example.chaenggim.domain.seed.service.SeedService;
import org.example.chaenggim.domain.visit.dto.CommitmentItem;
import org.example.chaenggim.domain.visit.dto.QuestionItem;
import org.example.chaenggim.domain.visit.dto.VisitCreateRequest;
import org.example.chaenggim.domain.visit.dto.VisitResponse;
import org.example.chaenggim.domain.visit.dto.VisitUpdateRequest;
import org.example.chaenggim.domain.visit.entity.Visit;
import org.example.chaenggim.domain.visit.repository.VisitRepository;
import org.example.chaenggim.domain.visit.service.VisitService;
import org.example.chaenggim.global.config.TestClockConfig;
import org.example.chaenggim.global.error.BusinessException;
import org.example.chaenggim.global.error.ErrorCode;
import org.example.chaenggim.global.error.FieldErrorItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

/** 09 테스트 명세 3절 S-1 ~ S-14. 시드 입력 후 실행. 고정 Clock T = 2026-11-09. */
@SpringBootTest
@Transactional
@Import(TestClockConfig.class)
class ServiceIntegrationTest {

    private static final LocalDate T = LocalDate.of(2026, 11, 9);

    @Autowired SeedService seedService;
    @Autowired HomeService homeService;
    @Autowired DoctorService doctorService;
    @Autowired VisitService visitService;
    @Autowired QuestionService questionService;
    @Autowired CommitmentService commitmentService;
    @Autowired DoctorRepository doctorRepository;
    @Autowired VisitRepository visitRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired CommitmentRepository commitmentRepository;

    Long d1, d2, d3, d4;
    Long v1, v2, v4;
    Long q1, q2, q4, q5;
    Long c1, c2, c3, c4;

    @BeforeEach
    void setUp() {
        seedService.seed();
        d1 = doctor("김가상");
        d2 = doctor("이예시");
        d3 = doctor("박샘플");
        d4 = doctor("최데모");
        v1 = visit(d1, T.minusDays(21));
        v2 = visit(d1, T.minusDays(7));
        v4 = visit(d3, T.minusDays(10));
        q1 = question("고령 환자 관련 로수젯 자료가 있는지 궁금하다고 하심");
        q2 = question("아모잘탄 용량별 제형 차이를 물어보심");
        q4 = question("환자용 복약 안내 자료가 있는지 물어보심");
        q5 = question("에페 출시 일정이 정해졌는지 물어보심");
        c1 = commitment("학술 행사 일정 공유");
        c2 = commitment("고령 환자 자료 요약본 전달");
        c3 = commitment("환자용 안내 자료 샘플 전달");
        c4 = commitment("학술 행사 안내문 전달");
    }

    @Test
    @DisplayName("S-1 홈: 08 문서 5절 기대 결과와 건수·순서·숫자가 일치")
    void home() {
        HomeResponse home = homeService.load();

        assertThat(home.today()).isEqualTo(T);
        assertThat(home.overdueCommitments()).extracting(CommitmentResponse::id).containsExactly(c2);
        assertThat(home.overdueCommitments().get(0).overdue()).isTrue();
        assertThat(home.dueTodayCommitments()).extracting(CommitmentResponse::id).containsExactly(c3);
        assertThat(home.dueTodayCommitments().get(0).dueToday()).isTrue();
        assertThat(home.upcomingDoctors())
                .extracting(u -> u.doctor().id(), HomeResponse.UpcomingDoctor::pastDue,
                        HomeResponse.UpcomingDoctor::readyAnswerCount,
                        HomeResponse.UpcomingDoctor::openCommitmentCount)
                .containsExactly(
                        tuple(d3, true, 0L, 1L),
                        tuple(d1, false, 1L, 1L),
                        tuple(d2, false, 1L, 1L));
        assertThat(home.questionCounts()).containsOnlyKeys(
                QuestionStatus.UNANSWERED, QuestionStatus.CHECKING, QuestionStatus.READY);
        assertThat(home.questionCounts().get(QuestionStatus.UNANSWERED)).isEqualTo(1L);
        assertThat(home.questionCounts().get(QuestionStatus.CHECKING)).isEqualTo(1L);
        assertThat(home.questionCounts().get(QuestionStatus.READY)).isEqualTo(2L);
    }

    @Test
    @DisplayName("S-2 D1 상세: 전달할 답변 Q1, 확인 필요 Q2, 남은 약속 C2, 최근 방문 V2, 방문 V2·V1, 완료 약속 C1")
    void doctorDetailD1() {
        DoctorDetailResponse detail = doctorService.get(d1);

        assertThat(detail.today()).isEqualTo(T);
        assertThat(detail.doctor().name()).isEqualTo("김가상");
        assertThat(detail.prep().readyQuestions()).extracting(QuestionResponse::id).containsExactly(q1);
        assertThat(detail.prep().openQuestions()).extracting(QuestionResponse::id).containsExactly(q2);
        assertThat(detail.prep().openCommitments()).extracting(CommitmentResponse::id).containsExactly(c2);
        assertThat(detail.prep().latestVisit().id()).isEqualTo(v2);
        assertThat(detail.visits()).extracting(VisitResponse::id).containsExactly(v2, v1);
        assertThat(detail.visits()).extracting(VisitResponse::questionCount, VisitResponse::commitmentCount)
                .containsExactly(tuple(2L, 1L), tuple(0L, 1L));
        assertThat(detail.deliveredQuestions()).isEmpty();
        assertThat(detail.doneCommitments()).extracting(CommitmentResponse::id).containsExactly(c1);
    }

    @Test
    @DisplayName("D3 상세: 전달 완료 질문 Q5, 방문의 hasDelivered = true")
    void doctorDetailD3() {
        DoctorDetailResponse detail = doctorService.get(d3);

        assertThat(detail.deliveredQuestions()).extracting(QuestionResponse::id).containsExactly(q5);
        assertThat(detail.visits().get(0).hasDelivered()).isTrue();
    }

    @Test
    @DisplayName("S-3 D4 상세: 모든 목록 빈 배열, latestVisit null")
    void doctorDetailD4() {
        DoctorDetailResponse detail = doctorService.get(d4);

        assertThat(detail.prep().readyQuestions()).isEmpty();
        assertThat(detail.prep().openQuestions()).isEmpty();
        assertThat(detail.prep().openCommitments()).isEmpty();
        assertThat(detail.prep().latestVisit()).isNull();
        assertThat(detail.visits()).isEmpty();
        assertThat(detail.deliveredQuestions()).isEmpty();
        assertThat(detail.doneCommitments()).isEmpty();
    }

    @Test
    @DisplayName("S-4 검색: '샘플' → D3, '내과' → D1만 (진료과는 검색 대상 아님), 빈 검색어는 전체")
    void search() {
        assertThat(doctorService.search("샘플")).extracting(DoctorSummaryResponse::id).containsExactly(d3);
        assertThat(doctorService.search("내과")).extracting(DoctorSummaryResponse::id).containsExactly(d1);
        assertThat(doctorService.search("  ")).hasSize(4);
    }

    @Test
    @DisplayName("S-5 D1 삭제 후 홈·목록·질문함 개수에서 D1과 그 질문·약속이 빠지고, D1 상세는 NOT_FOUND")
    void deleteDoctor() {
        doctorService.delete(d1);

        HomeResponse home = homeService.load();
        assertThat(home.overdueCommitments()).isEmpty();
        assertThat(home.upcomingDoctors()).extracting(u -> u.doctor().id()).containsExactly(d3, d2);
        assertThat(home.questionCounts().get(QuestionStatus.READY)).isEqualTo(1L);
        assertThat(home.questionCounts().get(QuestionStatus.CHECKING)).isEqualTo(0L);
        assertThat(doctorService.search(null)).extracting(DoctorSummaryResponse::id).doesNotContain(d1);
        assertThat(questionService.list("READY").items()).extracting(QuestionResponse::id).containsExactly(q4);
        assertThat(questionService.countByStatus().get(QuestionStatus.CHECKING)).isZero();
        assertThatThrownBy(() -> doctorService.get(d1))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
        assertThatThrownBy(() -> questionService.get(q1))
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("S-6 질문 2·약속 1·다음 예정일로 방문 생성 → 방문 1, 질문 2(미답변), 약속 1, 예정일 변경")
    void createVisit() {
        VisitCreateRequest request = new VisitCreateRequest(T, "  메모  ", T.plusDays(7),
                List.of(new QuestionItem(" 질문 A ", null), new QuestionItem("질문 B", null)),
                List.of(new CommitmentItem("약속 A", T.plusDays(3))));

        VisitResponse response = visitService.create(d4, request);

        assertThat(response.doctorId()).isEqualTo(d4);
        assertThat(response.memo()).isEqualTo("메모");
        assertThat(response.questionCount()).isEqualTo(2);
        assertThat(response.commitmentCount()).isEqualTo(1);
        assertThat(response.hasDelivered()).isFalse();
        DoctorDetailResponse detail = doctorService.get(d4);
        assertThat(detail.visits()).hasSize(1);
        assertThat(detail.prep().openQuestions())
                .extracting(QuestionResponse::content, QuestionResponse::status)
                .containsExactly(tuple("질문 A", QuestionStatus.UNANSWERED), tuple("질문 B", QuestionStatus.UNANSWERED));
        assertThat(detail.prep().openCommitments()).hasSize(1);
        assertThat(detail.doctor().nextVisitDate()).isEqualTo(T.plusDays(7));
    }

    @Test
    @DisplayName("S-7 다음 예정일 null로 방문 생성 → 의료진 예정일 유지")
    void createVisitKeepsNextVisitDate() {
        visitService.create(d1, new VisitCreateRequest(T, null, null, List.of(), List.of()));

        assertThat(doctorService.get(d1).doctor().nextVisitDate()).isEqualTo(T.plusDays(1));
    }

    @Test
    @DisplayName("S-8 방문 날짜 미래 + 약속 기한 과거 2개 → 필드 에러 3개를 한 번에")
    void createVisitValidation() {
        VisitCreateRequest request = new VisitCreateRequest(T.plusDays(1), null, null, List.of(),
                List.of(new CommitmentItem("a", T.minusDays(1)), new CommitmentItem("b", T.minusDays(2))));

        assertThatThrownBy(() -> visitService.create(d1, request))
                .isInstanceOfSatisfying(BusinessException.class, e -> {
                    assertThat(e.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED);
                    assertThat(e.getFieldErrors()).extracting(FieldErrorItem::field)
                            .containsExactly("visitDate", "commitments[0].dueDate", "commitments[1].dueDate");
                    assertThat(e.getFieldErrors()).extracting(FieldErrorItem::message)
                            .containsExactly("방문 날짜는 오늘 이후일 수 없습니다.",
                                    "약속 기한은 오늘 이전일 수 없습니다.", "약속 기한은 오늘 이전일 수 없습니다.");
                });
        assertThat(visitRepository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("S-9 V2 삭제 → Q1·Q2·C2·V2 삭제")
    void deleteVisit() {
        visitService.delete(v2);

        assertThat(visitRepository.findById(v2)).isEmpty();
        assertThat(questionRepository.findById(q1)).isEmpty();
        assertThat(questionRepository.findById(q2)).isEmpty();
        assertThat(commitmentRepository.findById(c2)).isEmpty();
        assertThat(visitRepository.count()).isEqualTo(3);
        assertThat(questionRepository.count()).isEqualTo(3);
        assertThat(commitmentRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("S-10 V4 삭제(Q5 전달 완료) → VISIT_HAS_DELIVERED, 아무것도 삭제 안 됨")
    void deleteVisitWithDelivered() {
        assertThatThrownBy(() -> visitService.delete(v4))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.VISIT_HAS_DELIVERED);

        assertThat(visitRepository.count()).isEqualTo(4);
        assertThat(questionRepository.count()).isEqualTo(5);
        assertThat(commitmentRepository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("S-11 V2 날짜 수정 → Q1 응답의 visitDate도 바뀜")
    void updateVisitDate() {
        visitService.update(v2, new VisitUpdateRequest(T.minusDays(6), "수정 메모"));

        assertThat(questionService.get(q1).visitDate()).isEqualTo(T.minusDays(6));
        assertThat(visitService.get(v2).doctor().id()).isEqualTo(d1);
    }

    @Test
    @DisplayName("방문 날짜를 미래로 수정하면 visitDate 필드 에러")
    void updateVisitFutureDate() {
        assertThatThrownBy(() -> visitService.update(v2, new VisitUpdateRequest(T.plusDays(1), null)))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getFieldErrors()).extracting(FieldErrorItem::field).containsExactly("visitDate"));
    }

    @Test
    @DisplayName("S-12 Q5(전달 완료) 삭제 → QUESTION_LOCKED")
    void deleteDeliveredQuestion() {
        assertThatThrownBy(() -> questionService.delete(q5))
                .isInstanceOfSatisfying(BusinessException.class, e -> {
                    assertThat(e.getErrorCode().getCode()).isEqualTo("QUESTION_LOCKED");
                    assertThat(e.getMessage()).isEqualTo("전달 완료된 질문은 삭제할 수 없습니다.");
                });
        assertThat(questionRepository.findById(q5)).isPresent();
    }

    @Test
    @DisplayName("S-13 C2(기한 T-2) 내용만 수정, 기한 그대로 → 성공(R5)")
    void updateCommitmentContentOnly() {
        CommitmentResponse response = commitmentService.update(c2,
                new CommitmentUpdateRequest("고령 환자 자료 요약본 전달(수정)", T.minusDays(2)));

        assertThat(response.content()).isEqualTo("고령 환자 자료 요약본 전달(수정)");
        assertThat(response.dueDate()).isEqualTo(T.minusDays(2));
        assertThat(response.overdue()).isTrue();
    }

    @Test
    @DisplayName("S-14 C4 기한을 어제로 수정 → dueDate 필드 에러")
    void updateCommitmentPastDueDate() {
        assertThatThrownBy(() -> commitmentService.update(c4, new CommitmentUpdateRequest("학술 행사 안내문 전달", T.minusDays(1))))
                .isInstanceOfSatisfying(BusinessException.class, e -> {
                    assertThat(e.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED);
                    assertThat(e.getFieldErrors())
                            .containsExactly(new FieldErrorItem("dueDate", "약속 기한은 오늘 이전일 수 없습니다."));
                });
    }

    @Test
    @DisplayName("완료한 약속 수정 → COMMITMENT_DONE, 완료 취소 후에는 수정 가능")
    void updateDoneCommitment() {
        assertThatThrownBy(() -> commitmentService.update(c1, new CommitmentUpdateRequest("x", T)))
                .extracting("errorCode").isEqualTo(ErrorCode.COMMITMENT_DONE);

        commitmentService.reopen(c1);
        assertThat(commitmentService.update(c1, new CommitmentUpdateRequest("x", T)).content()).isEqualTo("x");
    }

    @Test
    @DisplayName("질문 상태 흐름: 미답변 → 확인 시작 → 답변 → 전달 완료, 시각은 Clock 기준")
    void questionFlow() {
        Long q3 = question("3제 복합 고혈압약은 어떤 경우에 먼저 고려하는지 물어보심");

        assertThat(questionService.startChecking(q3).status()).isEqualTo(QuestionStatus.CHECKING);
        QuestionResponse answered = questionService.answer(q3,
                new AnswerRequest(" 답변 ", SourceType.CLINICAL, "  "));
        assertThat(answered.status()).isEqualTo(QuestionStatus.READY);
        assertThat(answered.answer()).isEqualTo("답변");
        assertThat(answered.sourceDetail()).isNull();
        assertThat(answered.answeredAt()).isEqualTo(T.atTime(9, 0));
        QuestionResponse delivered = questionService.deliver(q3);
        assertThat(delivered.status()).isEqualTo(QuestionStatus.DELIVERED);
        assertThat(delivered.deliveredAt()).isEqualTo(T.atTime(9, 0));
    }

    @Test
    @DisplayName("질문함: 잘못된 상태 값은 미답변, 전달 완료 탭은 전달 시각순, 개수는 4개 상태 모두")
    void questionList() {
        assertThat(questionService.list("WRONG").status()).isEqualTo(QuestionStatus.UNANSWERED);
        assertThat(questionService.list(null).status()).isEqualTo(QuestionStatus.UNANSWERED);
        assertThat(questionService.list("READY").items()).extracting(QuestionResponse::id).containsExactly(q1, q4);
        assertThat(questionService.list("DELIVERED").items()).extracting(QuestionResponse::id).containsExactly(q5);
        assertThat(questionService.countByStatus()).containsExactly(
                entry(QuestionStatus.UNANSWERED, 1L),
                entry(QuestionStatus.CHECKING, 1L),
                entry(QuestionStatus.READY, 2L),
                entry(QuestionStatus.DELIVERED, 1L));
    }

    @Test
    @DisplayName("P1 방문 작성 시 질문 제품 태그 저장, 질문함 제품 필터는 목록과 개수에 함께 적용, 잘못된 값은 전체")
    void productTagAndFilter() {
        visitService.create(d4, new VisitCreateRequest(T, null, null,
                List.of(new QuestionItem("로수젯 질문", ProductTag.ROSUZET), new QuestionItem("태그 없는 질문", null)),
                List.of()));

        var filtered = questionService.list("UNANSWERED", "ROSUZET");
        assertThat(filtered.product()).isEqualTo(ProductTag.ROSUZET);
        assertThat(filtered.items()).extracting(QuestionResponse::content, QuestionResponse::productTag)
                .containsExactly(tuple("로수젯 질문", ProductTag.ROSUZET));
        assertThat(filtered.counts()).containsExactly(
                entry(QuestionStatus.UNANSWERED, 1L), entry(QuestionStatus.CHECKING, 0L),
                entry(QuestionStatus.READY, 0L), entry(QuestionStatus.DELIVERED, 0L));

        var all = questionService.list("UNANSWERED", "WRONG");
        assertThat(all.product()).isNull();
        assertThat(all.items()).hasSize(3);
        assertThat(all.counts().get(QuestionStatus.UNANSWERED)).isEqualTo(3L);
        // 홈 질문 현황은 제품 필터와 무관
        assertThat(homeService.load().questionCounts().get(QuestionStatus.UNANSWERED)).isEqualTo(3L);
    }

    @Test
    @DisplayName("약속 완료 → 완료 취소, 삭제")
    void commitmentCompleteReopenDelete() {
        CommitmentResponse done = commitmentService.complete(c3);
        assertThat(done.done()).isTrue();
        assertThat(done.doneAt()).isEqualTo(T.atTime(9, 0));
        assertThat(done.dueToday()).isFalse();

        assertThat(commitmentService.reopen(c3).done()).isFalse();
        commitmentService.delete(c3);
        assertThat(commitmentRepository.findById(c3)).isEmpty();
    }

    // ---- 시드 엔티티 id 찾기 ----

    private Long doctor(String name) {
        return doctorRepository.findAll().stream().filter(d -> d.getName().equals(name))
                .map(Doctor::getId).findFirst().orElseThrow();
    }

    private Long visit(Long doctorId, LocalDate date) {
        return visitRepository.findActiveByDoctorId(doctorId).stream().filter(v -> v.getVisitDate().equals(date))
                .map(Visit::getId).findFirst().orElseThrow();
    }

    private Long question(String content) {
        return questionRepository.findAll().stream().filter(q -> q.getContent().equals(content))
                .map(Question::getId).findFirst().orElseThrow();
    }

    private Long commitment(String content) {
        return commitmentRepository.findAll().stream().filter(c -> c.getContent().equals(content))
                .map(Commitment::getId).findFirst().orElseThrow();
    }
}
