package org.example.chaenggim.domain.visit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.example.chaenggim.global.util.Texts;

/** 방문 기록 작성의 약속 한 행. 기한 과거 검사는 서비스에서 한다. */
public record CommitmentItem(
        @NotBlank(message = "약속 내용을 입력해 주세요.")
        @Size(max = 300, message = "약속은 300자 이하로 입력해 주세요.")
        String content,

        @NotNull(message = "약속 기한을 입력해 주세요.")
        LocalDate dueDate
) {

    public CommitmentItem {
        content = Texts.strip(content);
    }
}
