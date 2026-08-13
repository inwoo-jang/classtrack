package com.inwoo.classtrack.dto.dashboard;

import com.inwoo.classtrack.calendar.AcademicCalendar;
import com.inwoo.classtrack.domain.Course;
import com.inwoo.classtrack.domain.CourseStatus;

import java.time.LocalDate;
import java.util.Collection;

/** 대시보드 상단의 강의 집계. */
public record CourseStats(
        int total,
        int upcoming,
        int ongoing,
        int finished) {

    public static CourseStats of(
            Collection<Course> courses, AcademicCalendar calendar, LocalDate today) {
        int upcoming = count(courses, calendar, today, CourseStatus.UPCOMING);
        int ongoing = count(courses, calendar, today, CourseStatus.ONGOING);
        int finished = count(courses, calendar, today, CourseStatus.FINISHED);

        return new CourseStats(courses.size(), upcoming, ongoing, finished);
    }

    private static int count(
            Collection<Course> courses,
            AcademicCalendar calendar,
            LocalDate today,
            CourseStatus status) {
        return (int) courses.stream()
                .filter(course -> calendar.statusOf(course, today) == status)
                .count();
    }
}
