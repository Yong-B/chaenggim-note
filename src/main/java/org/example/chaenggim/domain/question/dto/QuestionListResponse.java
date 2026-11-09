package org.example.chaenggim.domain.question.dto;

import java.util.List;
import java.util.Map;
import org.example.chaenggim.domain.question.entity.ProductTag;
import org.example.chaenggim.domain.question.entity.QuestionStatus;

/** 질문함. product는 적용한 제품 필터(없으면 null). counts는 4개 상태 모두(0 포함), 제품 필터 적용. */
public record QuestionListResponse(
        QuestionStatus status,
        ProductTag product,
        Map<QuestionStatus, Long> counts,
        List<QuestionResponse> items
) {
}
