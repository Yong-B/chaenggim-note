package org.example.chaenggim.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 응답 error.code와 메시지 (02 도메인 규칙 6절, 05 API 명세 1절).
 * 상수 이름과 code는 대부분 같지만 QUESTION_DELETE_LOCKED의 code는 QUESTION_LOCKED다.
 */
@Getter
public enum ErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "입력값을 확인해 주세요."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "요청 형식이 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 정보를 찾을 수 없습니다."),
    QUESTION_NOT_UNANSWERED(HttpStatus.CONFLICT, "QUESTION_NOT_UNANSWERED", "미답변 상태의 질문만 확인을 시작할 수 있습니다."),
    QUESTION_NOT_READY(HttpStatus.CONFLICT, "QUESTION_NOT_READY", "답변 준비됨 상태의 질문만 전달 완료로 바꿀 수 있습니다."),
    QUESTION_LOCKED(HttpStatus.CONFLICT, "QUESTION_LOCKED", "전달 완료된 질문은 수정할 수 없습니다."),
    QUESTION_DELETE_LOCKED(HttpStatus.CONFLICT, "QUESTION_LOCKED", "전달 완료된 질문은 삭제할 수 없습니다."),
    VISIT_HAS_DELIVERED(HttpStatus.CONFLICT, "VISIT_HAS_DELIVERED", "전달 완료된 질문이 있는 방문 기록은 삭제할 수 없습니다."),
    COMMITMENT_DONE(HttpStatus.CONFLICT, "COMMITMENT_DONE", "완료한 약속은 완료를 취소한 뒤 수정할 수 있습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
