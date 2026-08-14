package com.inwoo.classtrack.dto.course;

import com.inwoo.classtrack.calendar.AcademicCalendar;
import com.inwoo.classtrack.domain.Course;
import com.inwoo.classtrack.domain.CourseStatus;

import java.time.LocalDate;
import java.util.List;

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
        List<String> technologies,
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
                // 트랜잭션 안에서 복사한다. 지연 컬렉션을 그대로 넘기면
                // open-in-view=false 라 직렬화 시점에 세션이 없어 터진다.
                List.copyOf(course.getTechnologies()),
                assignments);
    }
}
