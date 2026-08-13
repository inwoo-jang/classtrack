package com.inwoo.classtrack.logging;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 최근 로그를 메모리에 담아두는 고정 크기 링 버퍼.
 *
 * <p>Logback appender 는 Spring 컨테이너보다 먼저 만들어지므로 Bean 으로 주입받을 수 없다.
 * 그래서 appender 와 service 가 공유할 수 있도록 싱글턴으로 둔다.
 *
 * <p>로깅은 여러 요청 스레드에서 동시에 일어나므로 모든 접근을 동기화한다.
 * 여기는 "담아두는" 일만 하고, 필터링/검색은 {@code LogService} 가 맡는다.
 */
public final class LogBuffer {

    /** 이보다 오래된 로그는 밀려나간다. */
    public static final int CAPACITY = 500;

    private static final LogBuffer INSTANCE = new LogBuffer();

    private final Deque<LogEntry> entries = new ArrayDeque<>(CAPACITY);
    private long nextSequence = 1;

    private LogBuffer() {
    }

    public static LogBuffer getInstance() {
        return INSTANCE;
    }

    public synchronized void add(
            Instant timestamp,
            String level,
            String logger,
            String thread,
            String raw) {
        if (entries.size() >= CAPACITY) {
            entries.removeFirst();
        }
        entries.addLast(new LogEntry(
                nextSequence++, timestamp, level, logger, thread, raw));
    }

    /**
     * 현재 담긴 로그를 최신 것부터 복사해 돌려준다.
     *
     * <p>복사본을 주는 이유: 호출자가 스트림으로 훑는 동안 다른 스레드가 버퍼에 로그를 추가하면
     * {@link java.util.ConcurrentModificationException} 이 난다.
     */
    public synchronized List<LogEntry> snapshot() {
        List<LogEntry> copy = new ArrayList<>(entries.size());
        entries.descendingIterator().forEachRemaining(copy::add);
        return copy;
    }

    /** 마지막으로 부여된 sequence. 화면이 "새 로그만" 요청할 때 기준으로 쓴다. */
    public synchronized long latestSequence() {
        return nextSequence - 1;
    }

    public synchronized void clear() {
        entries.clear();
    }
}
