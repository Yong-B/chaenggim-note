package org.example.chaenggim.domain.visit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.chaenggim.domain.question.entity.ProductTag;
import org.example.chaenggim.global.util.Texts;

/** 방문 기록 작성의 질문 한 칸. productTag는 P1(T15) 전까지 받기만 하고 저장하지 않는다. */
public record QuestionItem(
        @NotBlank(message = "질문 내용을 입력해 주세요.")
        @Size(max = 500, message = "질문은 500자 이하로 입력해 주세요.")
        String content,

        ProductTag productTag
) {

    public QuestionItem {
        content = Texts.strip(content);
    }
}
