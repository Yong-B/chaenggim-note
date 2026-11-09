package org.example.chaenggim.domain.commitment.repository;

/** 의료진별 미완료 약속 수 (GROUP BY c.doctor.id). 홈 "남은 약속 N"용. */
public record DoctorOpenCommitmentCount(Long doctorId, Long count) {
}
