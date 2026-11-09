package com.chaenggim.commitment.domain;

import com.chaenggim.doctor.domain.Doctor;
import com.chaenggim.global.entity.BaseTimeEntity;
import com.chaenggim.global.error.InvalidStateException;
import com.chaenggim.visit.domain.Visit;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "commitment")
public class Commitment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;

    @Column(name = "content", length = 300, nullable = false)
    private String content;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "done", nullable = false)
    private boolean done;

    @Column(name = "done_at")
    private LocalDateTime doneAt;

    private Commitment(Visit visit, String content, LocalDate dueDate) {
        this.visit = visit;
        this.doctor = visit.getDoctor();
        this.content = content;
        this.dueDate = dueDate;
        this.done = false;
    }

    public static Commitment create(Visit visit, String content, LocalDate dueDate) {
        return new Commitment(visit, content, dueDate);
    }

    /** R4. 미완료일 때만 수정한다. R5(기한 과거 검사)는 서비스에서 한다. */
    public void update(String content, LocalDate dueDate) {
        if (done) {
            throw new InvalidStateException("COMMITMENT_DONE", "완료한 약속은 완료를 취소한 뒤 수정할 수 있습니다.");
        }
        this.content = content;
        this.dueDate = dueDate;
    }

    public void complete(LocalDateTime now) {
        if (done) {
            return;
        }
        this.done = true;
        this.doneAt = now;
    }

    public void reopen() {
        if (!done) {
            return;
        }
        this.done = false;
        this.doneAt = null;
    }

    public boolean isOverdue(LocalDate today) {
        return !done && dueDate.isBefore(today);
    }

    public boolean isDueToday(LocalDate today) {
        return !done && dueDate.isEqual(today);
    }
}
