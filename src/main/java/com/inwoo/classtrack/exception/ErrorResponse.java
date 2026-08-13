package com.inwoo.classtrack.exception;

import java.time.Instant;
import java.util.List;

/**
 * 모든 오류 응답의 공통 형태.
 *
 * <p>Spring 기본 오류 응답과 필드 이름을 맞춰뒀다. 프론트의 fetch 래퍼가
 * {@code errors[].defaultMessage} 를 먼저 읽고 없으면 {@code message} 를 쓰기 때문에,
 * 형태를 바꾸면 화면의 오류 문구가 깨진다.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> errors) {

    public record FieldError(String field, String defaultMessage) {
    }

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path, List.of());
    }
}
