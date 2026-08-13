package com.inwoo.classtrack.calendar;

import com.inwoo.classtrack.domain.Course;
import com.inwoo.classtrack.domain.CourseStatus;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 수업일 계산기.
 *
 * <p>{@code durationDays} 는 달력 날짜가 아니라 <b>수업일 수</b>다. 주말과 공휴일은 세지
 * 않는다. 그래서 종료일은 시작일에 더하기만 해서는 나오지 않고, 하루씩 걸어가며
 * 수업일만 세어야 한다.
 *
 * <p>이 계산을 한 곳에 모아둔 이유: 종료일·진행 상태·캘린더가 모두 같은 규칙을 써야 한다.
 * 프론트에서 따로 계산하면 서버가 내려준 값과 어긋난다.
 */
@Component
public class AcademicCalendar {

    /** 무한 루프 방지. 이보다 긴 강의는 없다고 본다. */
    private static final int MAX_SPAN_DAYS = 3650;

    private final Set<LocalDate> holidays;

    public AcademicCalendar(AcademicCalendarProperties properties) {
        this.holidays = Set.copyOf(properties.holidays());
    }

    /** 주말도 공휴일도 아닌 날. */
    public boolean isSessionDay(LocalDate date) {
        return !isWeekend(date) && !holidays.contains(date);
    }

    public boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    public boolean isHoliday(LocalDate date) {
        return holidays.contains(date);
    }

    /**
     * 실제 수업이 열리는 날짜들. 시작일이 휴일이면 그다음 수업일부터 센다.
     *
     * @return 오래된 순서로 정확히 {@code durationDays} 개 (한도에 걸리면 그보다 적을 수 있음)
     */
    public List<LocalDate> sessionDates(LocalDate startDate, int durationDays) {
        List<LocalDate> dates = new ArrayList<>(Math.max(durationDays, 0));

        LocalDate cursor = startDate;
        int walked = 0;
        while (dates.size() < durationDays && walked < MAX_SPAN_DAYS) {
            if (isSessionDay(cursor)) {
                dates.add(cursor);
            }
            cursor = cursor.plusDays(1);
            walked++;
        }
        return dates;
    }

    /** 마지막 수업일. 수업일이 하나도 없으면 시작일을 그대로 돌려준다. */
    public LocalDate endDateOf(Course course) {
        List<LocalDate> dates = sessionDates(course.getStartDate(), course.getDurationDays());
        return dates.isEmpty() ? course.getStartDate() : dates.get(dates.size() - 1);
    }

    /** 기준일 시점의 진행 상태. */
    public CourseStatus statusOf(Course course, LocalDate today) {
        if (today.isBefore(course.getStartDate())) {
            return CourseStatus.UPCOMING;
        }
        return today.isAfter(endDateOf(course)) ? CourseStatus.FINISHED : CourseStatus.ONGOING;
    }

    /**
     * 오늘이 몇 일차인지 (1부터). 아직 시작 전이면 0, 끝났으면 전체 일수.
     * 휴일에 조회하면 직전까지 진행된 일수를 돌려준다.
     */
    public int dayIndexOf(Course course, LocalDate today) {
        List<LocalDate> dates = sessionDates(course.getStartDate(), course.getDurationDays());

        int count = 0;
        for (LocalDate date : dates) {
            if (date.isAfter(today)) {
                break;
            }
            count++;
        }
        return count;
    }

    public Set<LocalDate> holidaysBetween(LocalDate from, LocalDate to) {
        return holidays.stream()
                .filter(date -> !date.isBefore(from) && !date.isAfter(to))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
