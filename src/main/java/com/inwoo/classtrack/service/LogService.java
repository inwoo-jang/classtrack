package com.inwoo.classtrack.service;

import com.inwoo.classtrack.logging.LogBuffer;
import com.inwoo.classtrack.logging.LogEntry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * 메모리 로그 버퍼 조회.
 *
 * <p>DB 를 쓰지 않으므로 Repository 가 없다. {@link LogBuffer} 가 저장소 역할을 하고,
 * 여기서는 "무엇을 보여줄지"의 규칙만 다룬다.
 */
@Service
public class LogService {

    /** 낮은 레벨부터. 인덱스 비교로 "이 레벨 이상"을 판단한다. */
    private static final List<String> LEVEL_ORDER =
            List.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR");

    private static final int MAX_LIMIT = LogBuffer.CAPACITY;

    /**
     * @param limit    최대 개수 (1..500 으로 잘린다)
     * @param after    이 sequence 보다 새 것만. null 이면 전체
     * @param minLevel 이 레벨 이상만 (예: "WARN"). null 이면 전체
     * @param keyword  메시지/로거 이름 부분 일치. null 이면 전체
     */
    public List<LogEntry> query(int limit, Long after, String minLevel, String keyword) {
        int cappedLimit = Math.clamp(limit, 1, MAX_LIMIT);
        int minRank = rankOf(minLevel);
        String needle = (keyword == null || keyword.isBlank())
                ? null
                : keyword.toLowerCase(Locale.ROOT);

        return LogBuffer.getInstance().snapshot().stream()
                .filter(entry -> after == null || entry.sequence() > after)
                .filter(entry -> rankOf(entry.level()) >= minRank)
                .filter(entry -> needle == null || matches(entry, needle))
                .limit(cappedLimit)
                .toList();
    }

    public long latestSequence() {
        return LogBuffer.getInstance().latestSequence();
    }

    public void clear() {
        LogBuffer.getInstance().clear();
    }

    /** 알 수 없는 레벨 이름은 0 으로 취급해 필터가 아무것도 걸러내지 않게 한다. */
    private static int rankOf(String level) {
        if (level == null) {
            return 0;
        }
        int index = LEVEL_ORDER.indexOf(level.toUpperCase(Locale.ROOT));
        return index == -1 ? 0 : index;
    }

    /** 원문 전체를 대상으로 검색한다 — 스택트레이스 안의 클래스명도 걸린다. */
    private static boolean matches(LogEntry entry, String needle) {
        return contains(entry.raw(), needle) || contains(entry.logger(), needle);
    }

    private static boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }
}
