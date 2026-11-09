package org.example.chaenggim.domain.question.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.example.chaenggim.domain.doctor.entity.Doctor;
import org.example.chaenggim.global.error.BusinessException;
import org.example.chaenggim.global.error.ErrorCode;
import org.example.chaenggim.domain.visit.entity.Visit;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuestionTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 9, 9, 0);

    private Visit visit;

    @BeforeEach
    void setUp() {
        Doctor doctor = Doctor.builder().name("김가상").hospital("가상내과의원").department("내과").build();
        visit = Visit.builder().doctor(doctor).visitDate(LocalDate.of(2026, 11, 2)).build();
    }

    private Question unanswered() {
        return Question.builder().visit(visit).content("질문 내용").build();
    }

    private Question ready() {
        Question q = unanswered();
        q.answer("답변", SourceType.CLINICAL, "자료", NOW);
        return q;
    }

    private Question delivered() {
        Question q = ready();
        q.deliver(NOW.plusHours(1));
        return q;
    }

    @Test
    @DisplayName("Q-1 생성 직후에는 미답변이고 답변·출처가 없으며 의료진은 방문의 의료진이다")
    void create() {
        Question q = unanswered();

        assertThat(q.getStatus()).isEqualTo(QuestionStatus.UNANSWERED);
        assertThat(q.getAnswer()).isNull();
        assertThat(q.getSourceType()).isNull();
        assertThat(q.getDoctor()).isSameAs(visit.getDoctor());
        assertThat(q.getVisit()).isSameAs(visit);
        assertThat(q.getProductTag()).isNull();
    }

    @Test
    @DisplayName("P1 제품 태그는 생성 시 선택해 저장한다")
    void createWithProductTag() {
        Question q = Question.builder().visit(visit).content("질문").productTag(ProductTag.AMOSARTAN).build();

        assertThat(q.getProductTag()).isEqualTo(ProductTag.AMOSARTAN);
        assertThat(q.getStatus()).isEqualTo(QuestionStatus.UNANSWERED);
    }

    @Test
    @DisplayName("Q-2 미답변 질문은 확인을 시작하면 확인 중이 된다")
    void startChecking() {
        Question q = unanswered();

        q.startChecking();

        assertThat(q.getStatus()).isEqualTo(QuestionStatus.CHECKING);
    }

    @Test
    @DisplayName("Q-3 확인 중인 질문은 다시 확인을 시작할 수 없다")
    void startCheckingTwice() {
        Question q = unanswered();
        q.startChecking();

        assertThatThrownBy(q::startChecking)
                .isInstanceOf(BusinessException.class)
                .hasMessage("미답변 상태의 질문만 확인을 시작할 수 있습니다.")
                .extracting("errorCode").isEqualTo(ErrorCode.QUESTION_NOT_UNANSWERED);
    }

    @Test
    @DisplayName("Q-4 미답변 질문에 답변하면 답변 준비됨이 되고 답변·출처·답변 시각이 저장된다")
    void answerUnanswered() {
        Question q = unanswered();

        q.answer("답변", SourceType.MEDICAL_TEAM, "학술팀 메일", NOW);

        assertThat(q.getStatus()).isEqualTo(QuestionStatus.READY);
        assertThat(q.getAnswer()).isEqualTo("답변");
        assertThat(q.getSourceType()).isEqualTo(SourceType.MEDICAL_TEAM);
        assertThat(q.getSourceDetail()).isEqualTo("학술팀 메일");
        assertThat(q.getAnsweredAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("Q-5 확인 중인 질문에 답변하면 답변 준비됨이 된다")
    void answerChecking() {
        Question q = unanswered();
        q.startChecking();

        q.answer("답변", SourceType.LABEL, null, NOW);

        assertThat(q.getStatus()).isEqualTo(QuestionStatus.READY);
    }

    @Test
    @DisplayName("Q-6 답변 준비됨 질문에 다시 답변하면 상태는 유지되고 답변과 답변 시각이 갱신된다")
    void answerReady() {
        Question q = ready();
        LocalDateTime later = NOW.plusDays(1);

        q.answer("새 답변", SourceType.LABEL, null, later);

        assertThat(q.getStatus()).isEqualTo(QuestionStatus.READY);
        assertThat(q.getAnswer()).isEqualTo("새 답변");
        assertThat(q.getSourceType()).isEqualTo(SourceType.LABEL);
        assertThat(q.getSourceDetail()).isNull();
        assertThat(q.getAnsweredAt()).isEqualTo(later);
    }

    @Test
    @DisplayName("Q-7 전달 완료 질문에는 답변할 수 없다")
    void answerDelivered() {
        Question q = delivered();

        assertThatThrownBy(() -> q.answer("답변", SourceType.LABEL, null, NOW))
                .isInstanceOf(BusinessException.class)
                .hasMessage("전달 완료된 질문은 수정할 수 없습니다.")
                .extracting("errorCode").isEqualTo(ErrorCode.QUESTION_LOCKED);
    }

    @Test
    @DisplayName("Q-8 답변 준비됨 질문을 전달하면 전달 완료가 되고 전달 시각이 저장된다")
    void deliver() {
        Question q = ready();
        LocalDateTime deliveredAt = NOW.plusHours(2);

        q.deliver(deliveredAt);

        assertThat(q.getStatus()).isEqualTo(QuestionStatus.DELIVERED);
        assertThat(q.getDeliveredAt()).isEqualTo(deliveredAt);
    }

    @Test
    @DisplayName("Q-9 미답변 질문은 전달 완료로 바꿀 수 없다")
    void deliverUnanswered() {
        Question q = unanswered();

        assertThatThrownBy(() -> q.deliver(NOW))
                .isInstanceOf(BusinessException.class)
                .hasMessage("답변 준비됨 상태의 질문만 전달 완료로 바꿀 수 있습니다.")
                .extracting("errorCode").isEqualTo(ErrorCode.QUESTION_NOT_READY);
    }

    @Test
    @DisplayName("전달 완료 질문을 다시 전달할 수 없다")
    void deliverTwice() {
        Question q = delivered();

        assertThatThrownBy(() -> q.deliver(NOW))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.QUESTION_NOT_READY);
    }

    @Test
    @DisplayName("Q-10 출처가 없거나 답변이 비어 있으면 IllegalArgumentException")
    void answerInvalidArguments() {
        Question q = unanswered();

        assertThatThrownBy(() -> q.answer("답변", null, null, NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> q.answer("  ", SourceType.LABEL, null, NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> q.answer(null, SourceType.LABEL, null, NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(q.getStatus()).isEqualTo(QuestionStatus.UNANSWERED);
    }

    @Test
    @DisplayName("Q-11 답변 준비됨 질문의 내용을 고치면 내용만 바뀌고 상태는 유지된다")
    void changeContentReady() {
        Question q = ready();

        q.changeContent("고친 내용");

        assertThat(q.getContent()).isEqualTo("고친 내용");
        assertThat(q.getStatus()).isEqualTo(QuestionStatus.READY);
    }

    @Test
    @DisplayName("Q-12 전달 완료 질문의 내용은 고칠 수 없다")
    void changeContentDelivered() {
        Question q = delivered();

        assertThatThrownBy(() -> q.changeContent("고친 내용"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("전달 완료된 질문은 수정할 수 없습니다.")
                .extracting("errorCode").isEqualTo(ErrorCode.QUESTION_LOCKED);
    }

    @Test
    @DisplayName("전달 완료 질문은 삭제할 수 없고, 그 외 상태는 삭제할 수 있다")
    void checkDeletable() {
        unanswered().checkDeletable();
        ready().checkDeletable();

        assertThatThrownBy(() -> delivered().checkDeletable())
                .isInstanceOf(BusinessException.class)
                .hasMessage("전달 완료된 질문은 삭제할 수 없습니다.")
                .extracting("errorCode").isEqualTo(ErrorCode.QUESTION_DELETE_LOCKED);
        assertThat(ErrorCode.QUESTION_DELETE_LOCKED.getCode()).isEqualTo("QUESTION_LOCKED");
    }
}
