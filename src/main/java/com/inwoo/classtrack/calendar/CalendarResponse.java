package com.inwoo.classtrack.calendar;

import java.time.LocalDate;
import java.util.List;

/**
 * 캘린더 화면에 필요한 것. 요청한 기간에 걸치는 수업일과 공휴일만 담는다.
 */
public record CalendarResponse(
        LocalDate from,
        LocalDate to,
        List<LocalDate> holidays,
        List<Session> sessions) {

    /** 특정 날짜에 열리는 강의 한 칸. */
    public record Session(
            Long courseId,
            String courseTitle,
            String subject,
            LocalDate date,
            /** 그 강의의 몇 일차인지 (1부터) */
            int dayIndex,
            int totalDays,
            boolean liveLecture) {
    }
}
