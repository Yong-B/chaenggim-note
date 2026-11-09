package org.example.chaenggim.domain.visit.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.example.chaenggim.global.util.Texts;

/** 방문 기록 수정(D15): 날짜·메모만. */
public record VisitUpdateRequest(
        @NotNull(message = "방문 날짜를 입력해 주세요.")
        LocalDate visitDate,

        @Size(max = 1000, message = "메모는 1000자 이하로 입력해 주세요.")
        String memo
) {

    public VisitUpdateRequest {
        memo = Texts.stripToNull(memo);
    }
}
