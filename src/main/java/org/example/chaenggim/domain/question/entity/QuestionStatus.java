package org.example.chaenggim.domain.question.entity;

import lombok.Getter;

@Getter
public enum QuestionStatus {
    UNANSWERED("미답변"),
    CHECKING("확인 중"),
    READY("답변 준비됨"),
    DELIVERED("전달 완료");

    private final String label;

    QuestionStatus(String label) {
        this.label = label;
    }
}
