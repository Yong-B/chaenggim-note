package org.example.chaenggim.global.response;

import java.util.List;
import org.example.chaenggim.global.error.ErrorCode;
import org.example.chaenggim.global.error.FieldErrorItem;

/** ApiResponse.error. fieldErrors는 항상 배열(없으면 []). */
public record ErrorBody(String code, String message, List<FieldErrorItem> fieldErrors) {

    public static ErrorBody of(ErrorCode errorCode) {
        return of(errorCode, List.of());
    }

    public static ErrorBody of(ErrorCode errorCode, List<FieldErrorItem> fieldErrors) {
        return new ErrorBody(errorCode.getCode(), errorCode.getMessage(), List.copyOf(fieldErrors));
    }
}
