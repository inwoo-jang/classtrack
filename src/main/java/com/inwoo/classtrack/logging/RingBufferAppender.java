package com.inwoo.classtrack.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.time.Instant;

/**
 * 로그를 콘솔에 찍는 것과 별개로 {@link LogBuffer} 에도 쌓는 Logback appender.
 *
 * <p>{@link PatternLayout} 을 그대로 써서 콘솔과 동일한 문자열을 만든다.
 * 직접 문자열을 조립하지 않는 이유는 예외 스택트레이스 처리 때문이다 —
 * {@code %ex} 가 원인 예외(caused by) 사슬까지 알아서 펼쳐준다.
 */
public class RingBufferAppender extends AppenderBase<ILoggingEvent> {

    /** Spring Boot 기본 콘솔 출력과 같은 모양. 색상 코드만 뺐다. */
    private static final String PATTERN =
            "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{39} : %msg%n%ex";

    private PatternLayout layout;

    @Override
    public void start() {
        layout = new PatternLayout();
        layout.setContext(getContext());
        layout.setPattern(PATTERN);
        layout.start();
        super.start();
    }

    @Override
    public void stop() {
        if (layout != null) {
            layout.stop();
        }
        super.stop();
    }

    @Override
    protected void append(ILoggingEvent event) {
        LogBuffer.getInstance().add(
                Instant.ofEpochMilli(event.getTimeStamp()),
                event.getLevel().toString(),
                shortName(event.getLoggerName()),
                event.getThreadName(),
                // 끝의 개행만 걷어낸다. 스택트레이스 안의 줄바꿈은 그대로 둔다.
                stripTrailingNewline(layout.doLayout(event)));
    }

    /** com.inwoo.classtrack.service.CourseService -> CourseService */
    private static String shortName(String loggerName) {
        int lastDot = loggerName.lastIndexOf('.');
        return lastDot == -1 ? loggerName : loggerName.substring(lastDot + 1);
    }

    private static String stripTrailingNewline(String text) {
        return text.endsWith(System.lineSeparator())
                ? text.substring(0, text.length() - System.lineSeparator().length())
                : text.stripTrailing();
    }
}
