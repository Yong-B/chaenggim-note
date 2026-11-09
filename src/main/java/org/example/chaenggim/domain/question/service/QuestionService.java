package org.example.chaenggim.domain.question.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.chaenggim.domain.question.dto.AnswerRequest;
import org.example.chaenggim.domain.question.dto.QuestionListResponse;
import org.example.chaenggim.domain.question.dto.QuestionResponse;
import org.example.chaenggim.domain.question.dto.QuestionUpdateRequest;
import org.example.chaenggim.domain.question.entity.Question;
import org.example.chaenggim.domain.question.entity.QuestionStatus;
import org.example.chaenggim.domain.question.repository.QuestionRepository;
import org.example.chaenggim.domain.question.repository.StatusCount;
import org.example.chaenggim.global.error.BusinessException;
import org.example.chaenggim.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final Clock clock;
    private final QuestionRepository questionRepository;

    /** 7.3 질문함. status가 없거나 잘못된 값이면 UNANSWERED. */
    public QuestionListResponse list(String statusParam) {
        QuestionStatus status = parseStatus(statusParam);
        List<Question> questions = status == QuestionStatus.DELIVERED
                ? questionRepository.findActiveDelivered()
                : questionRepository.findActiveByStatusOrderByVisitDate(status);

        return new QuestionListResponse(
                status,
                countByStatus(),
                questions.stream().map(QuestionResponse::from).toList());
    }

    /** 숨김 의료진 제외, 4개 상태 모두 포함(없으면 0). */
    public Map<QuestionStatus, Long> countByStatus() {
        Map<QuestionStatus, Long> counts = new EnumMap<>(QuestionStatus.class);
        for (QuestionStatus s : QuestionStatus.values()) {
            counts.put(s, 0L);
        }
        for (StatusCount c : questionRepository.countActiveByStatus()) {
            counts.put(c.status(), c.count());
        }
        return counts;
    }

    public QuestionResponse get(Long id) {
        return QuestionResponse.from(findActive(id));
    }

    @Transactional
    public QuestionResponse updateContent(Long id, QuestionUpdateRequest request) {
        Question question = findActive(id);
        question.changeContent(request.content());
        return QuestionResponse.from(question);
    }

    @Transactional
    public QuestionResponse startChecking(Long id) {
        Question question = findActive(id);
        question.startChecking();
        return QuestionResponse.from(question);
    }

    @Transactional
    public QuestionResponse answer(Long id, AnswerRequest request) {
        Question question = findActive(id);
        question.answer(request.answer(), request.sourceType(), request.sourceDetail(), LocalDateTime.now(clock));
        return QuestionResponse.from(question);
    }

    @Transactional
    public QuestionResponse deliver(Long id) {
        Question question = findActive(id);
        question.deliver(LocalDateTime.now(clock));
        return QuestionResponse.from(question);
    }

    @Transactional
    public void delete(Long id) {
        Question question = findActive(id);
        question.checkDeletable();
        questionRepository.delete(question);
    }

    private Question findActive(Long id) {
        return questionRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private static QuestionStatus parseStatus(String value) {
        if (value != null) {
            for (QuestionStatus s : QuestionStatus.values()) {
                if (s.name().equals(value)) {
                    return s;
                }
            }
        }
        return QuestionStatus.UNANSWERED;
    }
}
