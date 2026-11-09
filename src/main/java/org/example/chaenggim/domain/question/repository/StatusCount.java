package org.example.chaenggim.domain.question.repository;

import org.example.chaenggim.domain.question.entity.QuestionStatus;

/** 상태별 질문 개수 (GROUP BY q.status). */
public record StatusCount(QuestionStatus status, Long count) {
}
