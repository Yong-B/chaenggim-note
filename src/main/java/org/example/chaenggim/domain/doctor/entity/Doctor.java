package org.example.chaenggim.domain.doctor.entity;

import org.example.chaenggim.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "doctor")
public class Doctor extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @Column(name = "hospital", length = 50, nullable = false)
    private String hospital;

    @Column(name = "department", length = 30, nullable = false)
    private String department;

    @Column(name = "memo", length = 500)
    private String memo;

    @Column(name = "next_visit_date")
    private LocalDate nextVisitDate;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private Doctor(String name, String hospital, String department, String memo, LocalDate nextVisitDate) {
        this.name = name;
        this.hospital = hospital;
        this.department = department;
        this.memo = memo;
        this.nextVisitDate = nextVisitDate;
    }

    /** 의료진 수정 화면용. nextVisitDate가 null이면 예정일을 지운다. */
    public void update(String name, String hospital, String department, String memo, LocalDate nextVisitDate) {
        this.name = name;
        this.hospital = hospital;
        this.department = department;
        this.memo = memo;
        this.nextVisitDate = nextVisitDate;
    }

    /** 방문 기록 화면용(D13). null이면 아무것도 바꾸지 않는다. */
    public void changeNextVisitDate(LocalDate date) {
        if (date == null) {
            return;
        }
        this.nextVisitDate = date;
    }

    /** 숨김 처리(D14). 이미 삭제됐으면 아무것도 하지 않는다. */
    public void delete(LocalDateTime now) {
        if (isDeleted()) {
            return;
        }
        this.deletedAt = now;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
