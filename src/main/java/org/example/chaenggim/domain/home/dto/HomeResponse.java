package org.example.chaenggim.domain.home.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.example.chaenggim.domain.commitment.dto.CommitmentResponse;
import org.example.chaenggim.domain.doctor.dto.DoctorRef;
import org.example.chaenggim.domain.doctor.entity.Doctor;
import org.example.chaenggim.domain.question.entity.QuestionStatus;

/** GET /api/home. questionCounts는 UNANSWERED·CHECKING·READY 3개. */
public record HomeResponse(
        LocalDate today,
        List<CommitmentResponse> overdueCommitments,
        List<CommitmentResponse> dueTodayCommitments,
        List<UpcomingDoctor> upcomingDoctors,
        Map<QuestionStatus, Long> questionCounts
) {

    public record UpcomingDoctor(
            DoctorRef doctor,
            String department,
            LocalDate nextVisitDate,
            boolean pastDue,
            long readyAnswerCount,
            long openCommitmentCount
    ) {

        public static UpcomingDoctor of(Doctor doctor, LocalDate today, long readyAnswerCount,
                                        long openCommitmentCount) {
            return new UpcomingDoctor(
                    DoctorRef.from(doctor),
                    doctor.getDepartment(),
                    doctor.getNextVisitDate(),
                    doctor.getNextVisitDate().isBefore(today),
                    readyAnswerCount,
                    openCommitmentCount);
        }
    }
}
