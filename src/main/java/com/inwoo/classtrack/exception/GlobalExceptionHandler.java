package com.inwoo.classtrack.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * 예외를 HTTP 상태 코드로 옮기는 한 곳.
 *
 * <p>이게 없으면 서비스가 던진 예외가 그대로 올라가 전부 500 이 된다.
 * "없는 강의를 조회했다"는 클라이언트 잘못이므로 404 여야 하고, 서버 오류가 아니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 리소스 없음 → 404. 흔한 상황이므로 스택트레이스는 남기지 않는다. */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NotFoundException e, HttpServletRequest request) {
        log.info("404 {} - {}", request.getRequestURI(), e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(), "Not Found", e.getMessage(),
                request.getRequestURI()));
    }

    /** @Valid 검증 실패 → 400. DTO 에 적어둔 한국어 메시지를 그대로 올려보낸다. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        List<ErrorResponse.FieldError> fields = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new ErrorResponse.FieldError(f.getField(), f.getDefaultMessage()))
                .toList();

        // 필드 오류가 아닌 클래스 레벨 검증(@AssertTrue)은 globalErrors 로 들어온다
        List<ErrorResponse.FieldError> globals = e.getBindingResult().getGlobalErrors().stream()
                .map(g -> new ErrorResponse.FieldError(g.getObjectName(), g.getDefaultMessage()))
                .toList();

        List<ErrorResponse.FieldError> all = new java.util.ArrayList<>(fields);
        all.addAll(globals);

        String summary = all.isEmpty()
                ? "요청 값이 올바르지 않습니다."
                : String.join("\n", all.stream().map(ErrorResponse.FieldError::defaultMessage).toList());

        log.info("400 {} - {}", request.getRequestURI(), summary.replace('\n', '/'));

        return ResponseEntity.badRequest().body(new ErrorResponse(
                Instant.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request",
                summary, request.getRequestURI(), all));
    }

    /** 그 밖의 잘못된 인자 → 400 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException e, HttpServletRequest request) {
        log.warn("400 {} - {}", request.getRequestURI(), e.getMessage());

        return ResponseEntity.badRequest().body(ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "Bad Request", e.getMessage(),
                request.getRequestURI()));
    }

    /**
     * 예상하지 못한 예외 → 500.
     * 여기는 진짜 버그이므로 스택트레이스를 남기되, 클라이언트에는 내부 사정을 알리지 않는다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception e, HttpServletRequest request) {
        log.error("500 {} - {}", request.getRequestURI(), e.toString(), e);

        return ResponseEntity.internalServerError().body(ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                "서버에서 오류가 발생했습니다.", request.getRequestURI()));
    }
}
