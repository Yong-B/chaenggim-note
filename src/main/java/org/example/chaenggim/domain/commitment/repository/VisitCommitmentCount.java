package org.example.chaenggim.domain.commitment.repository;

/** 방문별 약속 수 (GROUP BY c.visit.id). VisitResponse의 commitmentCount용. */
public record VisitCommitmentCount(Long visitId, Long count) {
}
