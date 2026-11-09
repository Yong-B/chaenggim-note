package org.example.chaenggim.domain.question.repository;

/** 의료진별 답변 준비됨 질문 수 (GROUP BY q.doctor.id). 홈 "전달할 답변 N"용. */
public record DoctorReadyCount(Long doctorId, Long count) {
}
