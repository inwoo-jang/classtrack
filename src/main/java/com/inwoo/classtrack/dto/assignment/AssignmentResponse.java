package com.inwoo.classtrack.dto.assignment;

import com.inwoo.classtrack.domain.Assignment;
import com.inwoo.classtrack.domain.AssignmentStatus;
import com.inwoo.classtrack.domain.LinkStatus;

import java.time.LocalDateTime;

public record AssignmentResponse(
        Long id,
        Long courseId,
        String courseTitle,
        String title,
        String description,
        LocalDateTime dueDate,
        AssignmentStatus status,
        LocalDateTime submittedAt,
        String submissionUrl,
        LinkStatus linkStatus,
        LocalDateTime linkCheckedAt) {
    public static AssignmentResponse from(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getCourse().getId(),
                assignment.getCourse().getTitle(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDate(),
                assignment.getStatus(),
                assignment.getSubmittedAt(),
                assignment.getSubmissionUrl(),
                assignment.getLinkStatus(),
                assignment.getLinkCheckedAt());
    }
}
