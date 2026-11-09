package org.example.chaenggim.domain.visit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import org.example.chaenggim.domain.doctor.dto.DoctorRef;
import org.example.chaenggim.domain.visit.entity.Visit;

/**
 * 05 API 명세 2절 VisitResponse. hasDelivered가 true면 프론트는 삭제 버튼을 숨긴다(D16).
 * doctor는 GET /api/visits/{id}에서만 채운다(다른 응답에서는 JSON에 나오지 않음).
 */
public record VisitResponse(
        Long id,
        Long doctorId,
        LocalDate visitDate,
        String memo,
        long questionCount,
        long commitmentCount,
        boolean hasDelivered,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        DoctorRef doctor
) {

    public static VisitResponse of(Visit visit, long questionCount, long commitmentCount, boolean hasDelivered) {
        return new VisitResponse(visit.getId(), visit.getDoctor().getId(), visit.getVisitDate(), visit.getMemo(),
                questionCount, commitmentCount, hasDelivered, null);
    }

    public VisitResponse withDoctor(DoctorRef doctorRef) {
        return new VisitResponse(id, doctorId, visitDate, memo, questionCount, commitmentCount, hasDelivered, doctorRef);
    }
}
