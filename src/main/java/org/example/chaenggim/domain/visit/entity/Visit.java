package org.example.chaenggim.domain.visit.entity;

import org.example.chaenggim.domain.doctor.entity.Doctor;
import org.example.chaenggim.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "visit")
public class Visit extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "memo", length = 1000)
    private String memo;

    @Builder
    private Visit(Doctor doctor, LocalDate visitDate, String memo) {
        this.doctor = doctor;
        this.visitDate = visitDate;
        this.memo = memo;
    }

    /** D15. 날짜·메모만 수정한다. */
    public void update(LocalDate visitDate, String memo) {
        this.visitDate = visitDate;
        this.memo = memo;
    }
}
