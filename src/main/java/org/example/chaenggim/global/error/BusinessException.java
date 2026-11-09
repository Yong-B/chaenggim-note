package org.example.chaenggim.global.error;

import java.util.List;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<FieldErrorItem> fieldErrors;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, List.of());
    }

    public BusinessException(ErrorCode errorCode, List<FieldErrorItem> fieldErrors) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.fieldErrors = List.copyOf(fieldErrors);
    }

    /** 서비스 필드 검증 에러 1건 (VALIDATION_FAILED). */
    public static BusinessException fieldError(String field, String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, List.of(new FieldErrorItem(field, message)));
    }
}
