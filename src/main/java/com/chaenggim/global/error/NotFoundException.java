package com.chaenggim.global.error;

public class NotFoundException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "요청한 정보를 찾을 수 없습니다.";

    public NotFoundException() {
        this(DEFAULT_MESSAGE);
    }

    public NotFoundException(String message) {
        super(message);
    }
}
