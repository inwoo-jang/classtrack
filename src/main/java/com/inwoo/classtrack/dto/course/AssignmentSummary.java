package com.inwoo.classtrack.dto.course;

import com.inwoo.classtrack.domain.Assignment;
import com.inwoo.classtrack.domain.AssignmentStatus;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * 강의 카드에 표시할 과제 집계.
 * 목록 화면에서 강의마다 과제를 다시 조회하지 않아도 되도록 CourseResponse 에 함께 담는다.
 */
public record AssignmentSummary(
        int total,
        int todo,
        int inProgress,
        int completed,
        int overdue) {

    public static AssignmentSummary empty() {
        return new AssignmentSummary(0, 0, 0, 0, 0);
    }

    public static AssignmentSummary of(Collection<Assignment> assignments) {
        int todo = count(assignments, AssignmentStatus.TODO);
        int inProgress = count(assignments, AssignmentStatus.IN_PROGRESS);
        int completed = count(assignments, AssignmentStatus.COMPLETED);

        int overdue = (int) assignments.stream()
                .filter(AssignmentSummary::isOverdue)
                .count();

        return new AssignmentSummary(assignments.size(), todo, inProgress, completed, overdue);
    }

    private static int count(Collection<Assignment> assignments, AssignmentStatus status) {
        return (int) assignments.stream()
                .filter(assignment -> assignment.getStatus() == status)
                .count();
    }

    /** 아직 끝내지 않았는데 마감이 지난 과제. */
    private static boolean isOverdue(Assignment assignment) {
        return assignment.getStatus() != AssignmentStatus.COMPLETED
                && assignment.getDueDate() != null
                && assignment.getDueDate().isBefore(LocalDateTime.now());
    }
}
