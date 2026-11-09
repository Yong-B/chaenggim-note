package org.example.chaenggim.domain.question.entity;

import org.example.chaenggim.domain.doctor.entity.Doctor;
import org.example.chaenggim.global.entity.BaseTimeEntity;
import org.example.chaenggim.global.error.BusinessException;
import org.example.chaenggim.global.error.ErrorCode;
import org.example.chaenggim.domain.visit.entity.Visit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "question")
public class Question extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;

    @Column(name = "content", length = 500, nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_tag", length = 20)
    private ProductTag productTag;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private QuestionStatus status;

    @Column(name = "answer", length = 1000)
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 20)
    private SourceType sourceType;

    @Column(name = "source_detail", length = 200)
    private String sourceDetail;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /** doctor는 visit.getDoctor(), status는 UNANSWERED로 고정한다(빌더 인자 아님). */
    @Builder
    private Question(Visit visit, String content) {
        this.visit = visit;
        this.doctor = visit.getDoctor();
        this.content = content;
        this.status = QuestionStatus.UNANSWERED;
    }

    public void startChecking() {
        if (status != QuestionStatus.UNANSWERED) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_UNANSWERED);
        }
        this.status = QuestionStatus.CHECKING;
    }

    public void answer(String answer, SourceType sourceType, String sourceDetail, LocalDateTime now) {
        requireNotDelivered(ErrorCode.QUESTION_LOCKED);
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("answer must not be blank");
        }
        if (sourceType == null) {
            throw new IllegalArgumentException("sourceType must not be null");
        }
        this.answer = answer;
        this.sourceType = sourceType;
        this.sourceDetail = sourceDetail;
        this.answeredAt = now;
        this.status = QuestionStatus.READY;
    }

    public void deliver(LocalDateTime now) {
        if (status != QuestionStatus.READY) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_READY);
        }
        this.deliveredAt = now;
        this.status = QuestionStatus.DELIVERED;
    }

    public void changeContent(String content) {
        requireNotDelivered(ErrorCode.QUESTION_LOCKED);
        this.content = content;
    }

    /** 삭제 전에 서비스에서 호출한다. 전달 완료면 QUESTION_LOCKED. */
    public void checkDeletable() {
        requireNotDelivered(ErrorCode.QUESTION_DELETE_LOCKED);
    }

    public boolean isDelivered() {
        return status == QuestionStatus.DELIVERED;
    }

    private void requireNotDelivered(ErrorCode errorCode) {
        if (isDelivered()) {
            throw new BusinessException(errorCode);
        }
    }
}
