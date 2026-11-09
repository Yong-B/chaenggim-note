package org.example.chaenggim.domain.commitment.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.example.chaenggim.domain.commitment.entity.Commitment;
import org.example.chaenggim.domain.doctor.dto.DoctorRef;

/** overdue·dueToday는 서버가 today 기준으로 계산한다. */
public record CommitmentResponse(
        Long id,
        DoctorRef doctor,
        Long visitId,
        String content,
        LocalDate dueDate,
        boolean done,
        LocalDateTime doneAt,
        boolean overdue,
        boolean dueToday
) {

    public static CommitmentResponse from(Commitment commitment, LocalDate today) {
        return new CommitmentResponse(
                commitment.getId(),
                DoctorRef.from(commitment.getDoctor()),
                commitment.getVisit().getId(),
                commitment.getContent(),
                commitment.getDueDate(),
                commitment.isDone(),
                commitment.getDoneAt(),
                commitment.isOverdue(today),
                commitment.isDueToday(today));
    }
}
