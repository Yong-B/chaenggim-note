package org.example.chaenggim.global.util;

/** 요청 문자열 정리 (02 도메인 규칙 8절: 저장 전 strip(), 선택 필드의 빈 문자열은 null). */
public final class Texts {

    private Texts() {
    }

    /** 필수 필드용. 앞뒤 공백 제거 (빈 문자열은 그대로 두어 @NotBlank가 잡게 한다). */
    public static String strip(String value) {
        return value == null ? null : value.strip();
    }

    /** 선택 필드용. 앞뒤 공백 제거 후 비어 있으면 null. */
    public static String stripToNull(String value) {
        if (value == null) {
            return null;
        }
        String stripped = value.strip();
        return stripped.isEmpty() ? null : stripped;
    }
}
