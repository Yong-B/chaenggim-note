package org.example.chaenggim.domain.doctor.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DoctorTest {

    private static final LocalDate NEXT = LocalDate.of(2026, 11, 10);

    private Doctor doctor() {
        return Doctor.builder().name("김가상").hospital("가상내과의원").department("내과").memo("메모").nextVisitDate(NEXT).build();
    }

    @Test
    @DisplayName("D-1 방문 기록 화면에서 다음 방문 예정일을 비우면 기존 값을 유지한다")
    void changeNextVisitDateNull() {
        Doctor d = doctor();

        d.changeNextVisitDate(null);

        assertThat(d.getNextVisitDate()).isEqualTo(NEXT);
    }

    @Test
    @DisplayName("방문 기록 화면에서 다음 방문 예정일을 넣으면 바뀐다")
    void changeNextVisitDate() {
        Doctor d = doctor();

        d.changeNextVisitDate(NEXT.plusDays(7));

        assertThat(d.getNextVisitDate()).isEqualTo(NEXT.plusDays(7));
    }

    @Test
    @DisplayName("D-2 의료진 수정에서 다음 방문 예정일을 비우면 지워진다")
    void updateClearsNextVisitDate() {
        Doctor d = doctor();

        d.update("이예시", "예시가정의학과의원", "가정의학과", null, null);

        assertThat(d.getName()).isEqualTo("이예시");
        assertThat(d.getHospital()).isEqualTo("예시가정의학과의원");
        assertThat(d.getDepartment()).isEqualTo("가정의학과");
        assertThat(d.getMemo()).isNull();
        assertThat(d.getNextVisitDate()).isNull();
    }

    @Test
    @DisplayName("D-3 두 번 삭제해도 첫 삭제 시각이 그대로다")
    void deleteTwice() {
        Doctor d = doctor();
        LocalDateTime first = LocalDateTime.of(2026, 11, 9, 9, 0);

        assertThat(d.isDeleted()).isFalse();
        d.delete(first);
        d.delete(first.plusHours(1));

        assertThat(d.isDeleted()).isTrue();
        assertThat(d.getDeletedAt()).isEqualTo(first);
    }
}
