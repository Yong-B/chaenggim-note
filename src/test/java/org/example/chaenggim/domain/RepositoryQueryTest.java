package org.example.chaenggim.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.util.List;
import org.example.chaenggim.domain.commitment.entity.Commitment;
import org.example.chaenggim.domain.commitment.repository.CommitmentRepository;
import org.example.chaenggim.domain.doctor.entity.Doctor;
import org.example.chaenggim.domain.doctor.repository.DoctorRepository;
import org.example.chaenggim.domain.question.entity.Question;
import org.example.chaenggim.domain.question.entity.QuestionStatus;
import org.example.chaenggim.domain.question.entity.SourceType;
import org.example.chaenggim.domain.question.repository.QuestionRepository;
import org.example.chaenggim.domain.visit.entity.Visit;
import org.example.chaenggim.domain.visit.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryQueryTest {

    private static final LocalDate T = LocalDate.of(2026, 11, 9);

    @Autowired TestEntityManager em;
    @Autowired DoctorRepository doctorRepository;
    @Autowired VisitRepository visitRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired CommitmentRepository commitmentRepository;

    Doctor kim;      // 활성, 예정일 T+1
    Doctor park;     // 활성, 예정일 T-2 (지남)
    Doctor choi;     // 활성, 예정일 없음
    Doctor hidden;   // 숨김, 예정일 T
    Visit kimOld;    // T-21
    Visit kimNew;    // T-7
    Visit parkVisit; // T-10
    Visit hiddenVisit;
    Question qReady;
    Question qChecking;
    Question qUnanswered;
    Question qDelivered;
    Question qHidden;
    Commitment cOverdue;
    Commitment cOverdueEarlier;
    Commitment cToday;
    Commitment cLater;
    Commitment cDone;
    Commitment cHidden;

    @BeforeEach
    void setUp() {
        kim = persist(doctor("김가상", "가상내과의원", "내과", T.plusDays(1)));
        park = persist(doctor("박샘플", "샘플대학병원", "순환기내과", T.minusDays(2)));
        choi = persist(doctor("최데모", "데모종합병원", "종양내과", null));
        Doctor h = doctor("숨김의사", "숨김내과의원", "내과", T);
        h.delete(T.atTime(8, 0));
        hidden = persist(h);

        kimOld = persist(visit(kim, T.minusDays(21)));
        kimNew = persist(visit(kim, T.minusDays(7)));
        parkVisit = persist(visit(park, T.minusDays(10)));
        hiddenVisit = persist(visit(hidden, T.minusDays(1)));

        qReady = question(kimNew, "준비됨");
        qReady.answer("답", SourceType.CLINICAL, null, T.minusDays(3).atTime(16, 0));
        persist(qReady);
        qChecking = question(kimNew, "확인 중");
        qChecking.startChecking();
        persist(qChecking);
        qUnanswered = persist(question(kimOld, "미답변"));
        qDelivered = question(parkVisit, "전달 완료");
        qDelivered.answer("답", SourceType.LABEL, null, T.minusDays(8).atTime(10, 0));
        qDelivered.deliver(T.minusDays(6).atTime(14, 0));
        persist(qDelivered);
        qHidden = persist(question(hiddenVisit, "숨김 의료진 질문"));

        cOverdue = persist(commitment(kimNew, "기한 지남 1", T.minusDays(2)));
        cOverdueEarlier = persist(commitment(parkVisit, "기한 지남 2", T.minusDays(5)));
        cToday = persist(commitment(kimNew, "오늘까지", T));
        cLater = persist(commitment(parkVisit, "나중", T.plusDays(4)));
        cDone = commitment(kimOld, "완료", T.minusDays(20));
        cDone.complete(T.minusDays(20).atTime(18, 0));
        persist(cDone);
        cHidden = persist(commitment(hiddenVisit, "숨김 약속", T.minusDays(1)));

        em.flush();
        em.clear();
    }

    // ---- Doctor ----

    @Test
    @DisplayName("숨김 의료진은 id로 찾을 수 없다")
    void doctorFindActiveById() {
        assertThat(doctorRepository.findActiveById(kim.getId())).isPresent();
        assertThat(doctorRepository.findActiveById(hidden.getId())).isEmpty();
    }

    @Test
    @DisplayName("검색어가 없으면 활성 의료진 전체를 이름순으로")
    void searchAll() {
        assertThat(doctorRepository.searchActive(null)).extracting(Doctor::getName)
                .containsExactly("김가상", "박샘플", "최데모");
    }

    @Test
    @DisplayName("이름·병원으로 검색하고 진료과는 검색 대상이 아니다")
    void searchKeyword() {
        assertThat(doctorRepository.searchActive("샘플")).extracting(Doctor::getName).containsExactly("박샘플");
        assertThat(doctorRepository.searchActive("가상내과")).extracting(Doctor::getName).containsExactly("김가상");
        // "내과"는 김가상의 병원(가상내과의원)에만 해당. 박샘플의 진료과(순환기내과)는 대상 아님, 숨김 의료진 제외
        assertThat(doctorRepository.searchActive("내과")).extracting(Doctor::getName).containsExactly("김가상");
    }

    @Test
    @DisplayName("이번 주 방문 예정은 지난 예정일 포함, 예정일 없는 의료진과 숨김 의료진 제외, 예정일순")
    void upcoming() {
        assertThat(doctorRepository.findActiveUpcoming(T.plusDays(6))).extracting(Doctor::getName)
                .containsExactly("박샘플", "김가상");
        assertThat(doctorRepository.findActiveUpcoming(T)).extracting(Doctor::getName)
                .containsExactly("박샘플");
    }

    @Test
    @DisplayName("기본 count()는 숨김 의료진도 센다(시드 판단용)")
    void countIncludesHidden() {
        assertThat(doctorRepository.count()).isEqualTo(4);
    }

    // ---- Visit ----

    @Test
    @DisplayName("방문 기록은 최근 날짜순, 숨김 의료진의 방문은 찾을 수 없다")
    void visits() {
        assertThat(visitRepository.findActiveByDoctorId(kim.getId())).extracting(Visit::getId)
                .containsExactly(kimNew.getId(), kimOld.getId());
        assertThat(visitRepository.findActiveByDoctorId(hidden.getId())).isEmpty();
        assertThat(visitRepository.findActiveById(hiddenVisit.getId())).isEmpty();
        assertThat(visitRepository.findActiveById(kimNew.getId())).isPresent();
    }

    // ---- Question ----

    @Test
    @DisplayName("의료진 상세의 질문 목록: 전달할 답변 / 확인 필요(방문일순) / 전달 완료")
    void doctorQuestions() {
        assertThat(questionRepository.findReadyByDoctorId(kim.getId())).extracting(Question::getId)
                .containsExactly(qReady.getId());
        assertThat(questionRepository.findOpenByDoctorId(kim.getId())).extracting(Question::getId)
                .containsExactly(qUnanswered.getId(), qChecking.getId());
        assertThat(questionRepository.findDeliveredByDoctorId(park.getId())).extracting(Question::getId)
                .containsExactly(qDelivered.getId());
        assertThat(questionRepository.findOpenByDoctorId(hidden.getId())).isEmpty();
    }

    @Test
    @DisplayName("질문함 상태별 목록과 개수는 숨김 의료진을 제외한다")
    void questionBox() {
        assertThat(questionRepository.findActiveByStatusOrderByVisitDate(QuestionStatus.UNANSWERED))
                .extracting(Question::getId).containsExactly(qUnanswered.getId());
        assertThat(questionRepository.findActiveDelivered()).extracting(Question::getId)
                .containsExactly(qDelivered.getId());
        assertThat(questionRepository.countActiveByStatus())
                .extracting("status", "count")
                .containsExactlyInAnyOrder(
                        tuple(QuestionStatus.UNANSWERED, 1L),
                        tuple(QuestionStatus.CHECKING, 1L),
                        tuple(QuestionStatus.READY, 1L),
                        tuple(QuestionStatus.DELIVERED, 1L));
    }

    @Test
    @DisplayName("숨김 의료진의 질문은 id로 찾을 수 없다")
    void questionFindActiveById() {
        assertThat(questionRepository.findActiveById(qReady.getId())).isPresent();
        assertThat(questionRepository.findActiveById(qHidden.getId())).isEmpty();
    }

    @Test
    @DisplayName("의료진별 답변 준비됨 개수, 방문별 질문 수·전달 완료 수를 GROUP BY로 센다")
    void questionAggregates() {
        assertThat(questionRepository.countReadyByDoctorIds(List.of(kim.getId(), park.getId())))
                .extracting("doctorId", "count")
                .containsExactly(tuple(kim.getId(), 1L));
        assertThat(questionRepository.countByVisitIds(List.of(kimNew.getId(), kimOld.getId(), parkVisit.getId())))
                .extracting("visitId", "questionCount", "deliveredCount")
                .containsExactlyInAnyOrder(
                        tuple(kimNew.getId(), 2L, 0L),
                        tuple(kimOld.getId(), 1L, 0L),
                        tuple(parkVisit.getId(), 1L, 1L));
        // 숨김 의료진의 id를 넘겨도 집계에 나오지 않는다(R7)
        assertThat(questionRepository.countByVisitIds(List.of(hiddenVisit.getId()))).isEmpty();
        assertThat(commitmentRepository.countOpenByDoctorIds(List.of(hidden.getId()))).isEmpty();
        assertThat(questionRepository.existsByVisit_IdAndStatus(parkVisit.getId(), QuestionStatus.DELIVERED)).isTrue();
        assertThat(questionRepository.existsByVisit_IdAndStatus(kimNew.getId(), QuestionStatus.DELIVERED)).isFalse();
    }

    // ---- Commitment ----

    @Test
    @DisplayName("홈 약속: 기한 지남(기한순) / 오늘까지, 숨김 의료진 제외")
    void homeCommitments() {
        assertThat(commitmentRepository.findActiveOverdue(T)).extracting(Commitment::getId)
                .containsExactly(cOverdueEarlier.getId(), cOverdue.getId());
        assertThat(commitmentRepository.findActiveDueToday(T)).extracting(Commitment::getId)
                .containsExactly(cToday.getId());
    }

    @Test
    @DisplayName("의료진 상세 약속: 남은 약속(기한순) / 완료한 약속, 미완료 개수 집계")
    void doctorCommitments() {
        assertThat(commitmentRepository.findOpenByDoctorId(kim.getId())).extracting(Commitment::getId)
                .containsExactly(cOverdue.getId(), cToday.getId());
        assertThat(commitmentRepository.findDoneByDoctorId(kim.getId())).extracting(Commitment::getId)
                .containsExactly(cDone.getId());
        assertThat(commitmentRepository.countOpenByDoctorIds(List.of(kim.getId(), park.getId(), choi.getId())))
                .extracting("doctorId", "count")
                .containsExactlyInAnyOrder(tuple(kim.getId(), 2L), tuple(park.getId(), 2L));
        assertThat(commitmentRepository.countByVisitIds(List.of(kimNew.getId(), kimOld.getId())))
                .extracting("visitId", "count")
                .containsExactlyInAnyOrder(tuple(kimNew.getId(), 2L), tuple(kimOld.getId(), 1L));
        assertThat(commitmentRepository.findActiveById(cHidden.getId())).isEmpty();
    }

    @Test
    @DisplayName("방문의 질문·약속을 일괄 삭제한 뒤 방문을 삭제할 수 있다")
    void bulkDeleteByVisit() {
        assertThat(questionRepository.deleteByVisitId(kimNew.getId())).isEqualTo(2);
        assertThat(commitmentRepository.deleteByVisitId(kimNew.getId())).isEqualTo(2);
        visitRepository.deleteById(kimNew.getId());
        em.flush();

        assertThat(visitRepository.findActiveByDoctorId(kim.getId())).extracting(Visit::getId)
                .containsExactly(kimOld.getId());
        assertThat(questionRepository.findOpenByDoctorId(kim.getId())).extracting(Question::getId)
                .containsExactly(qUnanswered.getId());
    }

    // ---- fixtures ----

    private <E> E persist(E entity) {
        return em.persist(entity);
    }

    private static Doctor doctor(String name, String hospital, String department, LocalDate next) {
        return Doctor.builder().name(name).hospital(hospital).department(department).nextVisitDate(next).build();
    }

    private static Visit visit(Doctor doctor, LocalDate date) {
        return Visit.builder().doctor(doctor).visitDate(date).build();
    }

    private static Question question(Visit visit, String content) {
        return Question.builder().visit(visit).content(content).build();
    }

    private static Commitment commitment(Visit visit, String content, LocalDate due) {
        return Commitment.builder().visit(visit).content(content).dueDate(due).build();
    }
}
