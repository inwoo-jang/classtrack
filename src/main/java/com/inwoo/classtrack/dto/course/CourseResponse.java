package com.inwoo.classtrack.dto.course;

import com.inwoo.classtrack.calendar.AcademicCalendar;
import com.inwoo.classtrack.domain.Course;
import com.inwoo.classtrack.domain.CourseStatus;

import java.time.LocalDate;

public record CourseResponse(
        Long id,
        String title,
        String subject,
        String instructor,
        LocalDate startDate,
        /** 마지막 수업일. 주말·공휴일을 뺀 계산 결과이며 저장하지 않는다. */
        LocalDate endDate,
        /** 수업일 수 (달력 날짜 수가 아님) */
        Integer durationDays,
        String location,
        boolean liveLecture,
        String practiceProfessor,
        CourseStatus status,
        /** 오늘 기준 몇 일차인지. 시작 전이면 0 */
        int dayIndex,
        AssignmentSummary assignments) {

    /** 과제가 아직 없는 시점(예: 강의 생성 직후)에 쓴다. */
    public static CourseResponse from(Course course, AcademicCalendar calendar) {
        return from(course, AssignmentSummary.empty(), calendar, LocalDate.now());
    }

    public static CourseResponse from(
            Course course, AssignmentSummary assignments, AcademicCalendar calendar) {
        return from(course, assignments, calendar, LocalDate.now());
    }

    public static CourseResponse from(
            Course course,
            AssignmentSummary assignments,
            AcademicCalendar calendar,
            LocalDate today) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getSubject(),
                course.getInstructor(),
                course.getStartDate(),
                calendar.endDateOf(course),
                course.getDurationDays(),
                course.getLocation(),
                course.isLiveLecture(),
                course.getPracticeProfessor(),
                calendar.statusOf(course, today),
                calendar.dayIndexOf(course, today),
                assignments);
    }
}
