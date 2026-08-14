package com.inwoo.classtrack.dto.assignment;

import com.inwoo.classtrack.domain.Assignment;
import com.inwoo.classtrack.domain.AssignmentStatus;
import com.inwoo.classtrack.domain.AssignmentMode;
import com.inwoo.classtrack.domain.AssignmentRequirement;

import java.time.LocalDateTime;
import java.util.List;

public record AssignmentResponse(
        Long id,
        Long courseId,
        String courseTitle,
        String title,
        String description,
        LocalDateTime dueDate,
        AssignmentMode assignmentMode,
        AssignmentRequirement requirement,
        AssignmentStatus status,
        LocalDateTime submittedAt,
        List<AssignmentLinkResponse> submissionLinks) {
    public static AssignmentResponse from(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getCourse().getId(),
                assignment.getCourse().getTitle(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDate(),
                assignment.getAssignmentMode(),
                assignment.getRequirement(),
                assignment.getStatus(),
                assignment.getSubmittedAt(),
                assignment.getSubmissionLinks().stream()
                        .map(AssignmentLinkResponse::from)
                        .toList());
    }
}
