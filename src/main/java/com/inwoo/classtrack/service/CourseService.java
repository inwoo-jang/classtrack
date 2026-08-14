package com.inwoo.classtrack.service;

import com.inwoo.classtrack.exception.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inwoo.classtrack.calendar.AcademicCalendar;
import com.inwoo.classtrack.domain.Assignment;
import com.inwoo.classtrack.domain.Course;
import com.inwoo.classtrack.domain.CourseStatus;
import com.inwoo.classtrack.dto.course.AssignmentSummary;
import com.inwoo.classtrack.dto.course.CourseCreateRequest;
import com.inwoo.classtrack.dto.course.CourseResponse;
import com.inwoo.classtrack.dto.course.CourseUpdateRequest;
import com.inwoo.classtrack.repository.AssignmentRepository;
import com.inwoo.classtrack.repository.CourseRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 기본은 읽기 전용 트랜잭션이고, 데이터를 바꾸는 메서드에만 @Transactional 을 덧붙인다.
 * (AssignmentService 와 같은 규칙)
 */
@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;
    private final AcademicCalendar calendar;

    public CourseService(
            CourseRepository courseRepository,
            AssignmentRepository assignmentRepository,
            AcademicCalendar calendar) {
        this.courseRepository = courseRepository;
        this.assignmentRepository = assignmentRepository;
        this.calendar = calendar;
    }

    @Transactional
    public CourseResponse createCourse(CourseCreateRequest request) {
        Course course = new Course(
                request.title(),
                request.subject(),
                request.instructor(),
                request.startDate(),
                request.durationDays(),
                request.location(),
                request.liveLecture(),
                request.practiceProfessor(),
                request.technologies());

        Course savedCourse = courseRepository.save(course);

        return CourseResponse.from(savedCourse, calendar);
    }

    public List<CourseResponse> getCourses() {
        List<Course> courses = courseRepository.findAll();
        if (courses.isEmpty()) {
            return List.of();
        }

        // 강의마다 과제를 조회하면 N+1 이 되므로, 전체를 한 번에 받아 강의별로 나눈다.
        List<Long> courseIds = courses.stream().map(Course::getId).toList();
        Map<Long, List<Assignment>> byCourse = assignmentRepository
                .findAllByCourseIdIn(courseIds)
                .stream()
                .collect(Collectors.groupingBy(
                        assignment -> assignment.getCourse().getId()));

        return courses.stream()
                .map(course -> CourseResponse.from(
                        course,
                        AssignmentSummary.of(
                                byCourse.getOrDefault(course.getId(), List.of())),
                        calendar))
                .sorted(LISTING_ORDER)
                .toList();
    }

    /**
     * 목록 정렬 규칙.
     *
     * <ol>
     *   <li>아직 안 끝난 강의(진행 중·예정)를 먼저, 종료된 강의를 뒤로</li>
     *   <li>각 묶음 안에서는 시작일 오름차순 — 진행 중 → 미래, 종료된 것은 오래된 것 → 최근</li>
     *   <li>시작일이 같으면 등록 순서(id)</li>
     * </ol>
     *
     * <p>정렬을 DB 쿼리로 하지 않는 이유: 종료 여부가 저장된 컬럼이 아니라
     * 휴일을 뺀 계산 결과라서 SQL 의 ORDER BY 로는 표현할 수 없다.
     */
    private static final Comparator<CourseResponse> LISTING_ORDER =
            Comparator.comparingInt((CourseResponse c) ->
                            c.status() == CourseStatus.FINISHED ? 1 : 0)
                    .thenComparing(CourseResponse::startDate)
                    .thenComparing(CourseResponse::id);

    public CourseResponse getCourse(Long courseId) {
        Course course = findCourse(courseId);

        AssignmentSummary summary = AssignmentSummary.of(
                assignmentRepository.findAllByCourseIdOrderByDueDateAsc(courseId));

        return CourseResponse.from(course, summary, calendar);
    }

    /** PK 를 제외한 모든 필드를 교체한다. */
    @Transactional
    public CourseResponse updateCourse(Long courseId, CourseUpdateRequest request) {
        Course course = findCourse(courseId);

        course.update(
                request.title(),
                request.subject(),
                request.instructor(),
                request.startDate(),
                request.durationDays(),
                request.location(),
                request.liveLecture(),
                request.practiceProfessor(),
                request.technologies());

        // save() 를 부르지 않는다. 영속 상태 객체라 트랜잭션이 끝날 때
        // 변경 감지(dirty checking)로 UPDATE 가 나간다.
        AssignmentSummary summary = AssignmentSummary.of(
                assignmentRepository.findAllByCourseIdOrderByDueDateAsc(courseId));

        return CourseResponse.from(course, summary, calendar);
    }

    /**
     * 강의를 지운다. 딸린 과제도 함께 사라진다.
     *
     * <p>과제가 courses 를 FK 로 참조하므로 순서를 지켜야 한다. 강의를 먼저 지우면
     * 외래 키 제약에 걸린다.
     */
    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = findCourse(courseId);

        assignmentRepository.deleteAllByCourseId(course.getId());
        courseRepository.delete(course);
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> NotFoundException.course(courseId));
    }
}
