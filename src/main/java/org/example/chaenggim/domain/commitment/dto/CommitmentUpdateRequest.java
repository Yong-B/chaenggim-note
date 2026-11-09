package org.example.chaenggim.domain.commitment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.example.chaenggim.global.util.Texts;

/** 약속 수정. R5(기한을 바꿀 때 오늘 이전 불가)는 서비스에서 검사한다. */
public record CommitmentUpdateRequest(
        @NotBlank(message = "약속 내용을 입력해 주세요.")
        @Size(max = 300, message = "약속은 300자 이하로 입력해 주세요.")
        String content,

        @NotNull(message = "약속 기한을 입력해 주세요.")
        LocalDate dueDate
) {

    public CommitmentUpdateRequest {
        content = Texts.strip(content);
    }
}
