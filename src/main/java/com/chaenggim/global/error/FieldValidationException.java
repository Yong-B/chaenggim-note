package com.chaenggim.global.error;

import java.util.List;
import lombok.Getter;

@Getter
public class FieldValidationException extends RuntimeException {

    private final List<FieldErrorItem> errors;

    public FieldValidationException(List<FieldErrorItem> errors) {
        super("입력값을 확인해 주세요.");
        this.errors = List.copyOf(errors);
    }

    public FieldValidationException(String field, String message) {
        this(List.of(new FieldErrorItem(field, message)));
    }
}
