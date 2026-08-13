package com.inwoo.classtrack.service;

import com.inwoo.classtrack.calendar.AcademicCalendar;
import com.inwoo.classtrack.domain.Assignment;
import com.inwoo.classtrack.domain.AssignmentStatus;
import com.inwoo.classtrack.domain.Course;
import com.inwoo.classtrack.domain.CourseStatus;
import com.inwoo.classtrack.dto.assignment.AssignmentResponse;
import com.inwoo.classtrack.dto.course.AssignmentSummary;
import com.inwoo.classtrack.dto.course.CourseResponse;
import com.inwoo.classtrack.dto.dashboard.CourseStats;
import com.inwoo.classtrack.dto.dashboard.DashboardResponse;
import com.inwoo.classtrack.repository.AssignmentRepository;
import com.inwoo.classtrack.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    /** 첫 화면에 늘어놓을 "진행 중인 과제" 최대 개수. */
    private static final int IN_PROGRESS_LIMIT = 6;

    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;
    private final AcademicCalendar calendar;

    public DashboardService(
            CourseRepository courseRepository,
            AssignmentRepository assignmentRepository,
            AcademicCalendar calendar) {
        this.courseRepository = courseRepository;
        this.assignmentRepository = assignmentRepository;
        this.calendar = calendar;
    }

    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();

        List<Course> courses = courseRepository.findAll();
        List<Assignment> assignments = assignmentRepository.findAllByOrderByDueDateAsc();

        Map<Long, List<Assignment>> byCourse = assignments.stream()
                .collect(Collectors.groupingBy(a -> a.getCourse().getId()));

        List<CourseResponse> ongoing = courses.stream()
                .filter(course -> calendar.statusOf(course, today) == CourseStatus.ONGOING)
                .sorted(Comparator.comparing(Course::getStartDate))
                .map(course -> CourseResponse.from(
                        course,
                        AssignmentSummary.of(byCourse.getOrDefault(course.getId(), List.of())),
                        calendar,
                        today))
                .toList();

        // findAllByOrderByDueDateAsc 가 이미 마감일 순이므로 추가 정렬 없이 앞에서 자른다.
        List<AssignmentResponse> inProgress = assignments.stream()
                .filter(a -> a.getStatus() == AssignmentStatus.IN_PROGRESS)
                .limit(IN_PROGRESS_LIMIT)
                .map(AssignmentResponse::from)
                .toList();

        return new DashboardResponse(
                CourseStats.of(courses, calendar, today),
                AssignmentSummary.of(assignments),
                ongoing,
                inProgress);
    }
}
