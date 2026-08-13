package com.inwoo.classtrack.aspect;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 서비스 계층 호출을 가로채 실행 시간과 결과를 남긴다.
 *
 * <p>
 * 예전에는 각 서비스 안에 {@code log.info(...)} 를 직접 넣었지만,
 * 메서드마다 복붙해야 하고 빠뜨리기 쉬웠다. 로깅은 강의·과제 로직과 무관한
 * 횡단 관심사이므로 여기 한 곳으로 모은다.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /** 서비스 계층 전체 */
    @Pointcut("execution(* com.inwoo.classtrack.service..*(..))")
    public void serviceLayer() {
    }

    /**
     * LogService 는 제외한다.
     *
     * <p>
     * 로그 화면이 2초마다 LogService.query() 를 호출하는데, 그 호출까지 로깅하면
     * "로그를 읽었다"는 로그가 무한히 쌓여 버퍼가 자기 자신으로 가득 찬다.
     * 지우지 말 것.
     */
    // 포인트컷 정의
    @Pointcut("!within(com.inwoo.classtrack.service.LogService)")
    public void notLogging() {
    }

    // 어드바이스
    @Around("serviceLayer() && notLogging()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();

        // 지역 변수여야 한다. 필드에 두면 동시에 들어온 요청끼리 값을 덮어쓴다.
        // 벽시계(currentTimeMillis)가 아니라 단조 증가하는 nanoTime 을 쓴다.
        long startedAt = System.nanoTime();

        // @Around 는 proceed() 앞뒤를 모두 감싸므로 @Before 없이도 시작 시점을 찍을 수 있다.
        // 시작 로그의 쓸모는 "시작은 했는데 완료 로그가 없다" = 멈춘 메서드를 잡아내는 것.
        log.info("[AOP] {} 시작", signature);

        try {
            Object result = joinPoint.proceed();
            log.info("{} 완료 ({}ms)", signature, elapsedMillis(startedAt));
            return result;
        } catch (Throwable failure) {
            log.warn("{} 실패 ({}ms): {}", signature, elapsedMillis(startedAt), failure.toString());

            // 반드시 다시 던진다. 여기서 삼키면 예외가 사라져
            // 클라이언트가 오류 상황에서도 200 을 받게 된다.
            throw failure;
        }
    }

    @Before("@annotation(com.inwoo.classtrack.aspect.LogExecution)")
    public void logAnnotatedCall(JoinPoint joinPoint) {
        String signature = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        log.info("[AOP] {} 호출 - args: {}", signature, Arrays.toString(args));
    }

    // 헬퍼
    private static long elapsedMillis(long startedAtNanos) {
        return (System.nanoTime() - startedAtNanos) / 1_000_000;
    }
}
