package org.example.chaenggim.domain.visit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import org.example.chaenggim.global.util.Texts;

/** 방문 기록 작성. 질문·약속 각 0~10개. nextVisitDate가 null이면 의료진의 기존 예정일 유지(D13). */
public record VisitCreateRequest(
        @NotNull(message = "방문 날짜를 입력해 주세요.")
        LocalDate visitDate,

        @Size(max = 1000, message = "메모는 1000자 이하로 입력해 주세요.")
        String memo,

        LocalDate nextVisitDate,

        @Valid
        @Size(max = 10, message = "질문은 한 번에 10개까지 입력할 수 있습니다.")
        List<QuestionItem> questions,

        @Valid
        @Size(max = 10, message = "약속은 한 번에 10개까지 입력할 수 있습니다.")
        List<CommitmentItem> commitments
) {

    public VisitCreateRequest {
        memo = Texts.stripToNull(memo);
        questions = questions == null ? List.of() : questions;
        commitments = commitments == null ? List.of() : commitments;
    }
}
