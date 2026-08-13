package com.inwoo.classtrack.aspect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * "이 메서드가 어떤 값으로 호출됐는지 기록해줘" 라는 표시.
 *
 * <p>{@link LoggingAspect} 의 패키지 단위 포인트컷이 서비스 계층 전체를 이미 훑고 있으므로,
 * 이 애노테이션은 <b>그 범위 밖</b>(주로 컨트롤러)에서 골라 쓰기 위한 것이다.
 * 서비스 메서드에 붙이면 같은 호출이 두 번 기록되니 주의.
 *
 * <p>사용 예:
 * <pre>{@code
 * @LogExecution
 * @GetMapping("/{courseId}")
 * public ResponseEntity<CourseResponse> getCourse(@PathVariable Long courseId) { ... }
 * }</pre>
 *
 * <p><b>인자가 로그에 그대로 남는다.</b> 비밀번호·토큰·개인정보를 받는 메서드에는 붙이지 말 것.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME) // 런타임에 리플렉션으로 읽어야 하므로 SOURCE/CLASS 는 안 된다
@Target(ElementType.METHOD)
public @interface LogExecution {
}
