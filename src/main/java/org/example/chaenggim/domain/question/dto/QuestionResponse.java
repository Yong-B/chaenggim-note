package org.example.chaenggim.domain.question.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.example.chaenggim.domain.doctor.dto.DoctorRef;
import org.example.chaenggim.domain.question.entity.ProductTag;
import org.example.chaenggim.domain.question.entity.Question;
import org.example.chaenggim.domain.question.entity.QuestionStatus;
import org.example.chaenggim.domain.question.entity.SourceType;

/** visitDate = 받은 날(방문 날짜, R1). doctor·visit이 로딩된 상태에서 만든다. */
public record QuestionResponse(
        Long id,
        DoctorRef doctor,
        Long visitId,
        LocalDate visitDate,
        String content,
        ProductTag productTag,
        QuestionStatus status,
        String answer,
        SourceType sourceType,
        String sourceDetail,
        LocalDateTime answeredAt,
        LocalDateTime deliveredAt
) {

    public static QuestionResponse from(Question question) {
        return new QuestionResponse(
                question.getId(),
                DoctorRef.from(question.getDoctor()),
                question.getVisit().getId(),
                question.getVisit().getVisitDate(),
                question.getContent(),
                question.getProductTag(),
                question.getStatus(),
                question.getAnswer(),
                question.getSourceType(),
                question.getSourceDetail(),
                question.getAnsweredAt(),
                question.getDeliveredAt());
    }
}
