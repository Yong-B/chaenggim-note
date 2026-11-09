package org.example.chaenggim.domain.question.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.chaenggim.domain.question.dto.AnswerRequest;
import org.example.chaenggim.domain.question.dto.QuestionListResponse;
import org.example.chaenggim.domain.question.dto.QuestionResponse;
import org.example.chaenggim.domain.question.dto.QuestionUpdateRequest;
import org.example.chaenggim.domain.question.service.QuestionService;
import org.example.chaenggim.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    /** status가 없거나 잘못된 값이면 UNANSWERED. product가 없거나 잘못된 값이면 전체(P1). */
    @GetMapping
    public ResponseEntity<ApiResponse<QuestionListResponse>> list(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "product", required = false) String product) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.list(status, product)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.get(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateContent(@PathVariable Long id,
                                                                       @Valid @RequestBody QuestionUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.updateContent(id, request)));
    }

    @PostMapping("/{id}/start-checking")
    public ResponseEntity<ApiResponse<QuestionResponse>> startChecking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.startChecking(id)));
    }

    @PostMapping("/{id}/answer")
    public ResponseEntity<ApiResponse<QuestionResponse>> answer(@PathVariable Long id,
                                                                @Valid @RequestBody AnswerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.answer(id, request)));
    }

    @PostMapping("/{id}/deliver")
    public ResponseEntity<ApiResponse<QuestionResponse>> deliver(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.deliver(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
