package org.example.chaenggim.domain.commitment.entity;

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

class CommitmentTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 9);
    private static final LocalDateTime NOW = TODAY.atTime(9, 0);

    private Visit visit;

    @BeforeEach
    void setUp() {
        Doctor doctor = Doctor.builder().name("김가상").hospital("가상내과의원").department("내과").build();
        visit = Visit.builder().doctor(doctor).visitDate(TODAY.minusDays(7)).build();
    }

    @Test
    @DisplayName("생성 직후에는 미완료이고 의료진은 방문의 의료진이다")
    void create() {
        Commitment c = Commitment.builder().visit(visit).content("자료 전달").dueDate(TODAY).build();

        assertThat(c.isDone()).isFalse();
        assertThat(c.getDoneAt()).isNull();
        assertThat(c.getDoctor()).isSameAs(visit.getDoctor());
    }

    @Test
    @DisplayName("C-1 기한이 어제인 미완료 약속은 기한 지남이다")
    void overdue() {
        Commitment c = Commitment.builder().visit(visit).content("자료 전달").dueDate(TODAY.minusDays(1)).build();

        assertThat(c.isOverdue(TODAY)).isTrue();
        assertThat(c.isDueToday(TODAY)).isFalse();
    }

    @Test
    @DisplayName("C-2 기한이 오늘인 미완료 약속은 오늘까지다")
    void dueToday() {
        Commitment c = Commitment.builder().visit(visit).content("자료 전달").dueDate(TODAY).build();

        assertThat(c.isOverdue(TODAY)).isFalse();
        assertThat(c.isDueToday(TODAY)).isTrue();
    }

    @Test
    @DisplayName("C-3 기한이 어제라도 완료한 약속은 기한 지남이 아니다")
    void doneNotOverdue() {
        Commitment c = Commitment.builder().visit(visit).content("자료 전달").dueDate(TODAY.minusDays(1)).build();
        c.complete(NOW);

        assertThat(c.isOverdue(TODAY)).isFalse();
        assertThat(c.isDueToday(TODAY)).isFalse();
    }

    @Test
    @DisplayName("C-4 완료 후 완료를 취소하면 미완료가 되고 완료 시각이 지워진다")
    void completeThenReopen() {
        Commitment c = Commitment.builder().visit(visit).content("자료 전달").dueDate(TODAY).build();
        c.complete(NOW);

        c.reopen();

        assertThat(c.isDone()).isFalse();
        assertThat(c.getDoneAt()).isNull();
    }

    @Test
    @DisplayName("C-5 완료한 약속을 다시 완료해도 완료 시각은 그대로다")
    void completeTwice() {
        Commitment c = Commitment.builder().visit(visit).content("자료 전달").dueDate(TODAY).build();
        c.complete(NOW);

        c.complete(NOW.plusHours(3));

        assertThat(c.isDone()).isTrue();
        assertThat(c.getDoneAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("C-6 완료한 약속은 수정할 수 없다")
    void updateDone() {
        Commitment c = Commitment.builder().visit(visit).content("자료 전달").dueDate(TODAY).build();
        c.complete(NOW);

        assertThatThrownBy(() -> c.update("새 내용", TODAY.plusDays(1)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("완료한 약속은 완료를 취소한 뒤 수정할 수 있습니다.")
                .extracting("errorCode").isEqualTo(ErrorCode.COMMITMENT_DONE);
        assertThat(c.getContent()).isEqualTo("자료 전달");
    }

    @Test
    @DisplayName("미완료 약속은 내용과 기한을 수정할 수 있다")
    void updateOpen() {
        Commitment c = Commitment.builder().visit(visit).content("자료 전달").dueDate(TODAY).build();

        c.update("새 내용", TODAY.plusDays(2));

        assertThat(c.getContent()).isEqualTo("새 내용");
        assertThat(c.getDueDate()).isEqualTo(TODAY.plusDays(2));
    }

    @Test
    @DisplayName("미완료 약속의 완료 취소는 아무것도 바꾸지 않는다")
    void reopenOpen() {
        Commitment c = Commitment.builder().visit(visit).content("자료 전달").dueDate(TODAY).build();

        c.reopen();

        assertThat(c.isDone()).isFalse();
        assertThat(c.getDoneAt()).isNull();
    }
}
