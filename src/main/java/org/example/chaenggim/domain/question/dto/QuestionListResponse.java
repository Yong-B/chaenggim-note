package org.example.chaenggim.domain.question.dto;

import java.util.List;
import java.util.Map;
import org.example.chaenggim.domain.question.entity.QuestionStatus;

/** 질문함. counts는 4개 상태 모두(0 포함). */
public record QuestionListResponse(
        QuestionStatus status,
        Map<QuestionStatus, Long> counts,
        List<QuestionResponse> items
) {
}
