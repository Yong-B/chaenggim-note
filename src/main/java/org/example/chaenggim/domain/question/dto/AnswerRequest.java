package org.example.chaenggim.domain.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.chaenggim.domain.question.entity.SourceType;
import org.example.chaenggim.global.util.Texts;

/** 답변 저장. 출처 종류 필수(D3), 출처 상세는 선택. */
public record AnswerRequest(
        @NotBlank(message = "답변을 입력해 주세요.")
        @Size(max = 1000, message = "답변은 1000자 이하로 입력해 주세요.")
        String answer,

        @NotNull(message = "출처를 선택해 주세요.")
        SourceType sourceType,

        @Size(max = 200, message = "출처 상세는 200자 이하로 입력해 주세요.")
        String sourceDetail
) {

    public AnswerRequest {
        answer = Texts.strip(answer);
        sourceDetail = Texts.stripToNull(sourceDetail);
    }
}
