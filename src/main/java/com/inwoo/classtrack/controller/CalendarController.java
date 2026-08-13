package com.inwoo.classtrack.controller;

import com.inwoo.classtrack.calendar.CalendarResponse;
import com.inwoo.classtrack.calendar.CalendarService;
import com.inwoo.classtrack.dev.ApiDescription;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    /**
     * 기간 내 수업일과 공휴일. 보통 화면에 보이는 달 전체를 통째로 요청한다.
     */
    @ApiDescription("기간별 수업일·공휴일 조회")
    @GetMapping
    public ResponseEntity<CalendarResponse> getCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(calendarService.getCalendar(from, to));
    }
}
