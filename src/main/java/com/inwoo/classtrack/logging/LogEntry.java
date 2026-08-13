package com.inwoo.classtrack.logging;

import java.time.Instant;

/**
 * 화면에 뿌릴 로그 한 줄.
 *
 * @param raw 콘솔에 찍히는 것과 동일한 원문. 스택트레이스가 있으면 그대로 포함된다.
 *            화면은 이 값을 가공 없이 그대로 출력한다.
 */
public record LogEntry(
        long sequence,
        Instant timestamp,
        String level,
        /** 로거 이름의 마지막 조각 (예: AssignmentService). 필터·검색용 */
        String logger,
        String thread,
        String raw) {
}
