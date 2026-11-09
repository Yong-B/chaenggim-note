package org.example.chaenggim.global.response;

/** 모든 응답 본문 (05 API 명세 1절): { success, data, error }. */
public record ApiResponse<T>(boolean success, T data, ErrorBody error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /** 돌려줄 데이터가 없는 성공 (삭제, 초기화). */
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> fail(ErrorBody error) {
        return new ApiResponse<>(false, null, error);
    }
}
