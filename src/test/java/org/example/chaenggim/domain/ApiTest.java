package org.example.chaenggim.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManagerFactory;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.example.chaenggim.domain.commitment.entity.Commitment;
import org.example.chaenggim.domain.commitment.repository.CommitmentRepository;
import org.example.chaenggim.domain.doctor.entity.Doctor;
import org.example.chaenggim.domain.doctor.repository.DoctorRepository;
import org.example.chaenggim.domain.question.entity.Question;
import org.example.chaenggim.domain.question.repository.QuestionRepository;
import org.example.chaenggim.domain.seed.service.SeedService;
import org.example.chaenggim.domain.visit.repository.VisitRepository;
import org.example.chaenggim.global.config.TestClockConfig;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/** 09 테스트 명세 4절 W-1 ~ W-14. 시드 입력 후 실행. 응답은 ApiResponse { success, data, error }. */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@Transactional
@Import(TestClockConfig.class)
class ApiTest {

    @Autowired MockMvc mvc;
    @Autowired SeedService seedService;
    @Autowired DoctorRepository doctorRepository;
    @Autowired VisitRepository visitRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired CommitmentRepository commitmentRepository;
    @Autowired EntityManagerFactory entityManagerFactory;

    Long d1;
    Long d3;
    Long v2;
    Long v4;
    Long q3;   // 미답변
    Long q1;   // 답변 준비됨

    @BeforeEach
    void setUp() {
        seedService.seed();
        d1 = doctorId("김가상");
        d3 = doctorId("박샘플");
        v2 = visitRepository.findActiveByDoctorId(d1).get(0).getId();
        v4 = visitRepository.findActiveByDoctorId(d3).get(0).getId();
        q3 = questionId("3제 복합 고혈압약은 어떤 경우에 먼저 고려하는지 물어보심");
        q1 = questionId("고령 환자 관련 로수젯 자료가 있는지 궁금하다고 하심");
    }

    @Test
    @DisplayName("W-1 GET /api/home → 200, data.questionCounts.READY = 2")
    void home() throws Exception {
        mvc.perform(get("/api/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.today").value("2026-11-09"))
                .andExpect(jsonPath("$.data.questionCounts.READY").value(2))
                .andExpect(jsonPath("$.data.upcomingDoctors[0].pastDue").value(true));
    }

    @Test
    @DisplayName("홈 API 호출 시 SQL 10개 이하")
    void homeSqlCount() throws Exception {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        mvc.perform(get("/api/home")).andExpect(status().isOk());

        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("W-2 POST /api/doctors 이름 빈 값 → 400 VALIDATION_FAILED, fieldErrors[0] = name")
    void createDoctorBlankName() throws Exception {
        mvc.perform(json(post("/api/doctors"),
                        "{\"name\":\"  \",\"hospital\":\"가상병원\",\"department\":\"내과\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.error.fieldErrors.length()").value(1))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("name"))
                .andExpect(jsonPath("$.error.fieldErrors[0].message").value("이름을 입력해 주세요."));
    }

    @Test
    @DisplayName("W-3 POST /api/doctors 정상 → 201, Location 헤더, data.id")
    void createDoctor() throws Exception {
        mvc.perform(json(post("/api/doctors"),
                        "{\"name\":\"정가상\",\"hospital\":\"가상의원\",\"department\":\"내과\",\"memo\":\"\",\"nextVisitDate\":null}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/doctors/\\d+")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.memo").value(nullValue()));
    }

    @Test
    @DisplayName("W-4 GET /api/doctors/999999 → 404 NOT_FOUND")
    void doctorNotFound() throws Exception {
        mvc.perform(get("/api/doctors/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("요청한 정보를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.error.fieldErrors.length()").value(0));
    }

    @Test
    @DisplayName("W-5 방문 작성 질문 11개 → 400, field questions")
    void createVisitTooManyQuestions() throws Exception {
        String questions = IntStream.range(0, 11).mapToObj(i -> "{\"content\":\"질문" + i + "\"}")
                .collect(Collectors.joining(","));
        mvc.perform(json(post("/api/doctors/" + d1 + "/visits"),
                        "{\"visitDate\":\"2026-11-09\",\"questions\":[" + questions + "]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[*].field", hasItem("questions")))
                .andExpect(jsonPath("$.error.fieldErrors[*].message", hasItem("질문은 한 번에 10개까지 입력할 수 있습니다.")));
    }

    @Test
    @DisplayName("W-6 방문 작성 질문 내용 빈 값(인덱스 1) → 400, field questions[1].content")
    void createVisitBlankQuestion() throws Exception {
        mvc.perform(json(post("/api/doctors/" + d1 + "/visits"),
                        "{\"visitDate\":\"2026-11-09\",\"questions\":[{\"content\":\"질문\"},{\"content\":\"\"}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors.length()").value(1))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("questions[1].content"))
                .andExpect(jsonPath("$.error.fieldErrors[0].message").value("질문 내용을 입력해 주세요."));
    }

    @Test
    @DisplayName("W-7 방문 작성 visitDate \"2026-13-40\" → 400, 날짜 형식이 올바르지 않습니다.")
    void createVisitInvalidDate() throws Exception {
        mvc.perform(json(post("/api/doctors/" + d1 + "/visits"), "{\"visitDate\":\"2026-13-40\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("visitDate"))
                .andExpect(jsonPath("$.error.fieldErrors[0].message").value("날짜 형식이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("날짜 파싱 실패 경로: commitments[0].dueDate")
    void createVisitInvalidNestedDate() throws Exception {
        mvc.perform(json(post("/api/doctors/" + d1 + "/visits"),
                        "{\"visitDate\":\"2026-11-09\",\"commitments\":[{\"content\":\"a\",\"dueDate\":\"2026-02-30\"}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("commitments[0].dueDate"))
                .andExpect(jsonPath("$.error.fieldErrors[0].message").value("날짜 형식이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("JSON 형식 자체가 잘못되면 400 BAD_REQUEST")
    void malformedJson() throws Exception {
        mvc.perform(json(post("/api/doctors"), "{\"name\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error.message").value("요청 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.error.fieldErrors.length()").value(0));
    }

    @Test
    @DisplayName("서비스 검사 에러(방문 날짜 미래 + 약속 기한 과거)는 한 번에 400")
    void createVisitServiceValidation() throws Exception {
        mvc.perform(json(post("/api/doctors/" + d1 + "/visits"),
                        "{\"visitDate\":\"2026-11-10\",\"commitments\":[{\"content\":\"a\",\"dueDate\":\"2026-11-08\"}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors.length()").value(2))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("visitDate"))
                .andExpect(jsonPath("$.error.fieldErrors[1].field").value("commitments[0].dueDate"));
    }

    @Test
    @DisplayName("방문 작성 정상 → 201, data.questionCount")
    void createVisit() throws Exception {
        mvc.perform(json(post("/api/doctors/" + d1 + "/visits"),
                        "{\"visitDate\":\"2026-11-09\",\"memo\":\"메모\",\"nextVisitDate\":\"2026-11-16\","
                                + "\"questions\":[{\"content\":\"질문\",\"productTag\":null}],"
                                + "\"commitments\":[{\"content\":\"약속\",\"dueDate\":\"2026-11-12\"}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.questionCount").value(1))
                .andExpect(jsonPath("$.data.commitmentCount").value(1))
                .andExpect(jsonPath("$.data.hasDelivered").value(false))
                .andExpect(jsonPath("$.data.doctor").doesNotExist());
    }

    @Test
    @DisplayName("W-8 POST /api/questions/{미답변}/deliver → 409 QUESTION_NOT_READY")
    void deliverUnanswered() throws Exception {
        mvc.perform(post("/api/questions/" + q3 + "/deliver"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("QUESTION_NOT_READY"))
                .andExpect(jsonPath("$.error.message").value("답변 준비됨 상태의 질문만 전달 완료로 바꿀 수 있습니다."));
    }

    @Test
    @DisplayName("W-9 POST /api/questions/{id}/answer 출처 없음 → 400, field sourceType")
    void answerWithoutSource() throws Exception {
        mvc.perform(json(post("/api/questions/" + q3 + "/answer"), "{\"answer\":\"답변\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("sourceType"))
                .andExpect(jsonPath("$.error.fieldErrors[0].message").value("출처를 선택해 주세요."));
    }

    @Test
    @DisplayName("W-10 DELETE /api/visits/{V4} → 409 VISIT_HAS_DELIVERED")
    void deleteVisitWithDelivered() throws Exception {
        mvc.perform(delete("/api/visits/" + v4))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("VISIT_HAS_DELIVERED"))
                .andExpect(jsonPath("$.error.message").value("전달 완료된 질문이 있는 방문 기록은 삭제할 수 없습니다."));
    }

    @Test
    @DisplayName("W-11 GET /api/questions?status=WRONG → 200, data.status = UNANSWERED")
    void questionsWrongStatus() throws Exception {
        mvc.perform(get("/api/questions").param("status", "WRONG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNANSWERED"))
                .andExpect(jsonPath("$.data.counts.DELIVERED").value(1))
                .andExpect(jsonPath("$.data.items.length()").value(1));
    }

    @Test
    @DisplayName("W-12 DELETE /api/doctors/{id} → 200(data null) → GET → 404")
    void deleteDoctorThenGet() throws Exception {
        mvc.perform(delete("/api/doctors/" + d1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(nullValue()));
        mvc.perform(get("/api/doctors/" + d1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("W-13 POST /api/admin/reset → 200, success = true")
    void reset() throws Exception {
        mvc.perform(post("/api/admin/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        assertThat(doctorRepository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("W-14 날짜 JSON 형식: data.visitDate가 \"2026-11-02\" 형태 문자열, 시각은 ISO-8601")
    void dateFormat() throws Exception {
        mvc.perform(get("/api/visits/" + v2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.visitDate").value("2026-11-02"))
                .andExpect(jsonPath("$.data.doctor.name").value("김가상"));
        mvc.perform(get("/api/questions/" + q1))
                .andExpect(jsonPath("$.data.visitDate").value("2026-11-02"))
                .andExpect(jsonPath("$.data.answeredAt").value("2026-11-06T16:00:00"))
                .andExpect(jsonPath("$.data.sourceType").value("MEDICAL_TEAM"));
    }

    @Test
    @DisplayName("없는 경로 → 404 NOT_FOUND, 잘못된 id 형식 → 400 BAD_REQUEST")
    void unknownPath() throws Exception {
        mvc.perform(get("/api/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        mvc.perform(get("/api/doctors/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("나머지 엔드포인트 정상 응답: 의료진 목록·수정, 방문 수정·삭제, 질문 수정·확인 시작·답변·전달·삭제, 약속 전부")
    void otherEndpoints() throws Exception {
        mvc.perform(get("/api/doctors").param("q", "샘플"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        mvc.perform(json(put("/api/doctors/" + d1),
                        "{\"name\":\"김가상\",\"hospital\":\"가상내과의원\",\"department\":\"내과\",\"nextVisitDate\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nextVisitDate").value(nullValue()));
        mvc.perform(json(put("/api/visits/" + v2), "{\"visitDate\":\"2026-11-08\",\"memo\":\"수정\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.visitDate").value("2026-11-08"));

        mvc.perform(json(patch("/api/questions/" + q3),
                        "{\"content\":\"수정한 질문\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("수정한 질문"));
        mvc.perform(post("/api/questions/" + q3 + "/start-checking"))
                .andExpect(jsonPath("$.data.status").value("CHECKING"));
        mvc.perform(json(post("/api/questions/" + q3 + "/answer"),
                        "{\"answer\":\"답변\",\"sourceType\":\"CLINICAL\",\"sourceDetail\":\"RACING 연구 요약 자료\"}"))
                .andExpect(jsonPath("$.data.status").value("READY"));
        mvc.perform(post("/api/questions/" + q3 + "/deliver"))
                .andExpect(jsonPath("$.data.status").value("DELIVERED"));
        mvc.perform(delete("/api/questions/" + q3))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("QUESTION_LOCKED"))
                .andExpect(jsonPath("$.error.message").value("전달 완료된 질문은 삭제할 수 없습니다."));
        mvc.perform(delete("/api/questions/" + questionId("아모잘탄 용량별 제형 차이를 물어보심")))
                .andExpect(status().isOk());

        Long c4 = commitmentIdOf("학술 행사 안내문 전달");
        mvc.perform(get("/api/commitments/" + c4)).andExpect(jsonPath("$.data.dueDate").value("2026-11-13"));
        mvc.perform(json(put("/api/commitments/" + c4), "{\"content\":\"수정\",\"dueDate\":\"2026-11-08\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("dueDate"));
        mvc.perform(post("/api/commitments/" + c4 + "/complete")).andExpect(jsonPath("$.data.done").value(true));
        mvc.perform(json(put("/api/commitments/" + c4), "{\"content\":\"수정\",\"dueDate\":\"2026-11-13\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("COMMITMENT_DONE"));
        mvc.perform(post("/api/commitments/" + c4 + "/reopen")).andExpect(jsonPath("$.data.done").value(false));
        mvc.perform(delete("/api/commitments/" + c4)).andExpect(status().isOk());
        mvc.perform(delete("/api/visits/" + v2)).andExpect(status().isOk());
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder builder, String body) {
        return builder.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private Long doctorId(String name) {
        return doctorRepository.findAll().stream().filter(d -> d.getName().equals(name))
                .map(Doctor::getId).findFirst().orElseThrow();
    }

    private Long questionId(String content) {
        return questionRepository.findAll().stream().filter(q -> q.getContent().equals(content))
                .map(Question::getId).findFirst().orElseThrow();
    }

    private Long commitmentIdOf(String content) {
        return commitmentRepository.findAll().stream().filter(c -> c.getContent().equals(content))
                .map(Commitment::getId).findFirst().orElseThrow();
    }
}
