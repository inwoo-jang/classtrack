package com.inwoo.classtrack.service;

import com.inwoo.classtrack.exception.NotFoundException;

import com.inwoo.classtrack.domain.Assignment;
import com.inwoo.classtrack.domain.Course;
import com.inwoo.classtrack.domain.LinkStatus;
import com.inwoo.classtrack.dto.assignment.AssignmentCreateRequest;
import com.inwoo.classtrack.dto.assignment.AssignmentResponse;
import com.inwoo.classtrack.dto.assignment.AssignmentUpdateRequest;
import com.inwoo.classtrack.repository.AssignmentRepository;
import com.inwoo.classtrack.repository.CourseRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final ApplicationEventPublisher events;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            CourseRepository courseRepository,
            ApplicationEventPublisher events) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.events = events;
    }

    @Transactional
    public AssignmentResponse createAssignment(
            Long courseId,
            AssignmentCreateRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> NotFoundException.course(courseId));

        Assignment assignment = new Assignment(
                course,
                request.title(),
                request.description(),
                request.dueDate(),
                request.submissionUrl());

        Assignment savedAssignment = assignmentRepository.save(assignment);
        publishLinkCheck(savedAssignment);

        return AssignmentResponse.from(savedAssignment);
    }

    /** 진행 상태와 결과물 링크를 갱신한다. */
    @Transactional
    public AssignmentResponse updateAssignment(
            Long courseId,
            Long assignmentId,
            AssignmentUpdateRequest request) {
        Assignment assignment = assignmentRepository
                .findByIdAndCourseId(assignmentId, courseId)
                .orElseThrow(() -> NotFoundException.assignment(assignmentId));

        assignment.changeStatus(request.status());
        assignment.linkSubmission(request.submissionUrl());
        publishLinkCheck(assignment);

        // 영속 상태이므로 트랜잭션 종료 시 변경 감지(dirty checking)로 반영된다.
        return AssignmentResponse.from(assignment);
    }

    @Transactional
    public void deleteAssignment(Long courseId, Long assignmentId) {
        Assignment assignment = assignmentRepository
                .findByIdAndCourseId(assignmentId, courseId)
                .orElseThrow(() -> NotFoundException.assignment(assignmentId));

        assignmentRepository.delete(assignment);
    }

    /** 강의 구분 없이 전체 과제를 마감일 순으로. */
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAllByOrderByDueDateAsc()
                .stream()
                .map(AssignmentResponse::from)
                .toList();
    }

    public List<AssignmentResponse> getAssignments(Long courseId) {
        ensureCourseExists(courseId);

        return assignmentRepository.findAllByCourseIdOrderByDueDateAsc(courseId)
                .stream()
                .map(AssignmentResponse::from)
                .toList();
    }

    public AssignmentResponse getAssignment(Long courseId, Long assignmentId) {
        Assignment assignment = assignmentRepository
                .findByIdAndCourseId(assignmentId, courseId)
                .orElseThrow(() -> NotFoundException.assignment(assignmentId));

        return AssignmentResponse.from(assignment);
    }

    /**
     * 확인이 필요한 링크가 있으면 이벤트를 띄운다.
     * 실제 확인은 커밋 후 비동기로 일어나므로 이 메서드는 즉시 반환한다.
     */
    private void publishLinkCheck(Assignment assignment) {
        if (assignment.getLinkStatus() == LinkStatus.PENDING) {
            events.publishEvent(new SubmissionLinkChanged(
                    assignment.getId(), assignment.getSubmissionUrl()));
        }
    }

    private void ensureCourseExists(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw NotFoundException.course(courseId);
        }
    }
}
