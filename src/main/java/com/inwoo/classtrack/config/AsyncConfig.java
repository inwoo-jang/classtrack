package com.inwoo.classtrack.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 비동기 실행기.
 *
 * <p>@EnableAsync 가 있어야 @Async 가 프록시로 감싸진다. 없으면 애노테이션만 붙고
 * 그냥 동기로 실행된다 — 에러 없이 조용히.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.setTaskDecorator(mdcPropagating());
        executor.initialize();
        return executor;
    }

    /**
     * MDC 를 비동기 스레드로 복사한다.
     *
     * <p>MDC 는 ThreadLocal 이라 스레드가 바뀌면 비어 있다. 그대로 두면 비동기 작업의
     * 로그만 요청 맥락이 빠져서, 로그 화면에서 어느 요청에서 시작된 일인지 이어 볼 수 없다.
     *
     * <p>제출 시점의 맥락을 복사해 두었다가 실행 직전에 심고, 끝나면 지운다.
     * (스레드는 재사용되므로 정리하지 않으면 다음 작업이 남은 값을 물고 간다)
     */
    private static TaskDecorator mdcPropagating() {
        return runnable -> {
            Map<String, String> context = MDC.getCopyOfContextMap();

            return () -> {
                if (context != null) {
                    MDC.setContextMap(context);
                }
                try {
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            };
        };
    }
}
