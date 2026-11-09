package org.example.chaenggim.domain.question.repository;

/** 방문별 질문 수와 그중 전달 완료 수 (GROUP BY q.visit.id). VisitResponse의 questionCount·hasDelivered용. */
public record VisitQuestionCount(Long visitId, Long questionCount, Long deliveredCount) {
}
