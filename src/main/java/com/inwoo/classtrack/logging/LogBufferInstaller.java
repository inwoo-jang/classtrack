package com.inwoo.classtrack.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * {@link RingBufferAppender} 를 root logger 에 붙인다.
 *
 * <p>왜 logback-spring.xml 이 아니라 코드로 붙이는가:
 * spring-boot-devtools 는 애플리케이션 클래스를 매 재시작마다 새 클래스로더로 다시 읽는다.
 * XML 로 등록하면 appender 인스턴스가 만들어진 클래스로더와 컨트롤러/서비스가 로딩된
 * 클래스로더가 달라질 수 있고, 그러면 {@code LogBuffer.getInstance()} 가 서로 다른
 * 싱글턴을 가리켜 버퍼가 늘 비어 보인다.
 *
 * <p>Spring Bean 으로 등록해 컨텍스트가 뜬 뒤에 붙이면 appender 와 서비스가 항상 같은
 * 클래스로더를 쓴다.
 */
@Component
public class LogBufferInstaller {

    private static final String APPENDER_NAME = "RING_BUFFER";

    @PostConstruct
    void install() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);

        // LoggerContext 는 devtools 재시작 사이에도 살아남는다.
        // 이전 클래스로더가 남긴 appender 를 떼어내지 않으면 로그가 두 번 쌓인다.
        Appender<ILoggingEvent> stale = root.getAppender(APPENDER_NAME);
        if (stale != null) {
            stale.stop();
            root.detachAppender(stale);
        }

        RingBufferAppender appender = new RingBufferAppender();
        appender.setName(APPENDER_NAME);
        appender.setContext(context);
        appender.start();

        root.addAppender(appender);
    }
}
