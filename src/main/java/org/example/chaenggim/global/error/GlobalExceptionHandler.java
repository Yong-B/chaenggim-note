package org.example.chaenggim.global.error;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.chaenggim.global.response.ApiResponse;
import org.example.chaenggim.global.response.ErrorBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** 05 API 명세 1.1절 에러 응답. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    static final String DATE_FORMAT_MESSAGE = "날짜 형식이 올바르지 않습니다.";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        return respond(e.getErrorCode(), e.getFieldErrors());
    }

    /** Bean Validation 실패. 한 필드에 에러가 여럿이면 첫 번째만. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> firstByField = new LinkedHashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            firstByField.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        List<FieldErrorItem> items = firstByField.entrySet().stream()
                .map(entry -> new FieldErrorItem(entry.getKey(), entry.getValue()))
                .toList();
        return respond(ErrorCode.VALIDATION_FAILED, items);
    }

    /** 날짜 파싱 실패는 필드 에러, 그 외 JSON 형식 오류는 BAD_REQUEST. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        if (e.getCause() instanceof InvalidFormatException ife && ife.getTargetType() == LocalDate.class) {
            return respond(ErrorCode.VALIDATION_FAILED,
                    List.of(new FieldErrorItem(path(ife.getPath()), DATE_FORMAT_MESSAGE)));
        }
        return respond(ErrorCode.BAD_REQUEST, List.of());
    }

    /** 경로 변수 타입 불일치(예: /api/doctors/abc). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return respond(ErrorCode.BAD_REQUEST, List.of());
    }

    /** 없는 경로, 지원하지 않는 메서드. */
    @ExceptionHandler({NoResourceFoundException.class, HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(Exception e) {
        return respond(ErrorCode.NOT_FOUND, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return respond(ErrorCode.INTERNAL_ERROR, List.of());
    }

    private static ResponseEntity<ApiResponse<Void>> respond(ErrorCode errorCode, List<FieldErrorItem> fieldErrors) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(ErrorBody.of(errorCode, fieldErrors)));
    }

    /** Jackson 경로 → "commitments[0].dueDate" 형식. */
    static String path(List<JsonMappingException.Reference> references) {
        StringBuilder sb = new StringBuilder();
        for (JsonMappingException.Reference ref : references) {
            if (ref.getFieldName() != null) {
                if (!sb.isEmpty()) {
                    sb.append('.');
                }
                sb.append(ref.getFieldName());
            } else if (ref.getIndex() >= 0) {
                sb.append('[').append(ref.getIndex()).append(']');
            }
        }
        return sb.toString();
    }
}
