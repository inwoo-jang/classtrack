package com.inwoo.classtrack.exception;

/**
 * 요청한 리소스가 없을 때. HTTP 404 로 매핑된다.
 *
 * <p>예전에는 {@code IllegalArgumentException} 을 던졌는데, 그건 "인자가 잘못됐다"는
 * 뜻이라 "없다"와 구분되지 않았다. 예외 타입 자체가 의미를 담아야
 * {@link GlobalExceptionHandler} 가 상태 코드를 정할 수 있다.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException course(Long courseId) {
        return new NotFoundException("강의를 찾을 수 없습니다. id=" + courseId);
    }

    public static NotFoundException assignment(Long assignmentId) {
        return new NotFoundException("과제를 찾을 수 없습니다. id=" + assignmentId);
    }
}
