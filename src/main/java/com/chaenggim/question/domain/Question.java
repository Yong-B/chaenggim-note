package com.chaenggim.question.domain;

import com.chaenggim.doctor.domain.Doctor;
import com.chaenggim.global.entity.BaseTimeEntity;
import com.chaenggim.global.error.InvalidStateException;
import com.chaenggim.visit.domain.Visit;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "question")
public class Question extends BaseTimeEntity {

    private static final String LOCKED_CODE = "QUESTION_LOCKED";
    private static final String LOCKED_EDIT_MESSAGE = "전달 완료된 질문은 수정할 수 없습니다.";
    private static final String LOCKED_DELETE_MESSAGE = "전달 완료된 질문은 삭제할 수 없습니다.";

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

    private Question(Visit visit, String content) {
        this.visit = visit;
        this.doctor = visit.getDoctor();
        this.content = content;
        this.status = QuestionStatus.UNANSWERED;
    }

    public static Question create(Visit visit, String content) {
        return new Question(visit, content);
    }

    public void startChecking() {
        if (status != QuestionStatus.UNANSWERED) {
            throw new InvalidStateException("QUESTION_NOT_UNANSWERED", "미답변 상태의 질문만 확인을 시작할 수 있습니다.");
        }
        this.status = QuestionStatus.CHECKING;
    }

    public void answer(String answer, SourceType sourceType, String sourceDetail, LocalDateTime now) {
        requireNotDelivered(LOCKED_EDIT_MESSAGE);
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
            throw new InvalidStateException("QUESTION_NOT_READY", "답변 준비됨 상태의 질문만 전달 완료로 바꿀 수 있습니다.");
        }
        this.deliveredAt = now;
        this.status = QuestionStatus.DELIVERED;
    }

    public void changeContent(String content) {
        requireNotDelivered(LOCKED_EDIT_MESSAGE);
        this.content = content;
    }

    /** 삭제 전에 서비스에서 호출한다. 전달 완료면 QUESTION_LOCKED. */
    public void checkDeletable() {
        requireNotDelivered(LOCKED_DELETE_MESSAGE);
    }

    public boolean isDelivered() {
        return status == QuestionStatus.DELIVERED;
    }

    private void requireNotDelivered(String message) {
        if (isDelivered()) {
            throw new InvalidStateException(LOCKED_CODE, message);
        }
    }
}
