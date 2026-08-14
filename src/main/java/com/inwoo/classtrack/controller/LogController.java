package com.inwoo.classtrack.controller;

import com.inwoo.classtrack.dev.ApiDescription;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inwoo.classtrack.logging.LogEntry;
import com.inwoo.classtrack.service.LogService;

/**
 * 로그 보드. 스택트레이스와 요청 인자가 담기므로 구현 현황과 같은 스위치로 묶는다.
 */
@ConditionalOnProperty(prefix = "app.dev", name = "enabled", havingValue = "true")
@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * 최근 로그. 화면이 주기적으로 폴링하므로 {@code after} 로 새 것만 받아갈 수 있다.
     *
     * @param after 마지막으로 받은 sequence. 생략하면 전체
     * @param level 이 레벨 이상만 (TRACE/DEBUG/INFO/WARN/ERROR)
     * @param q     메시지·로거 이름 부분 검색어
     */
    @ApiDescription("로그 조회 (필터·검색·폴링)")
    @GetMapping
    public ResponseEntity<List<LogEntry>> getLogs(
            @RequestParam(defaultValue = "200") int limit,
            @RequestParam(required = false) Long after,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(logService.query(limit, after, level, q));
    }

    /** 버퍼를 비운다. 돌려줄 본문이 없으므로 204 No Content. */
    @ApiDescription("로그 버퍼 비우기")
    @DeleteMapping
    public ResponseEntity<Void> clear() {
        logService.clear();
        return ResponseEntity.noContent().build();
    }
}
