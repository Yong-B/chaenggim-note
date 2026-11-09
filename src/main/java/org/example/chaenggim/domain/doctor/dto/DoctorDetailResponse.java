package org.example.chaenggim.domain.doctor.dto;

import java.time.LocalDate;
import java.util.List;
import org.example.chaenggim.domain.commitment.dto.CommitmentResponse;
import org.example.chaenggim.domain.question.dto.QuestionResponse;
import org.example.chaenggim.domain.visit.dto.VisitResponse;

/** GET /api/doctors/{id}. prep = 방문 전 준비 영역(F8). */
public record DoctorDetailResponse(
        DoctorResponse doctor,
        LocalDate today,
        Prep prep,
        List<VisitResponse> visits,
        List<QuestionResponse> deliveredQuestions,
        List<CommitmentResponse> doneCommitments
) {

    public record Prep(
            List<QuestionResponse> readyQuestions,
            List<QuestionResponse> openQuestions,
            List<CommitmentResponse> openCommitments,
            VisitResponse latestVisit
    ) {
    }
}
