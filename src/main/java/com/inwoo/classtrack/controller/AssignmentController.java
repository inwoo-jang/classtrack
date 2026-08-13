package com.inwoo.classtrack.controller;

import com.inwoo.classtrack.dev.ApiDescription;

import com.inwoo.classtrack.aspect.LogExecution;
import com.inwoo.classtrack.dto.assignment.AssignmentCreateRequest;
import com.inwoo.classtrack.dto.assignment.AssignmentResponse;
import com.inwoo.classtrack.dto.assignment.AssignmentUpdateRequest;
import com.inwoo.classtrack.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/assignments")
public class AssignmentController {

        private final AssignmentService assignmentService;

        public AssignmentController(AssignmentService assignmentService) {
                this.assignmentService = assignmentService;
        }

        @ApiDescription("과제 등록")
        @LogExecution
        @PostMapping
        public ResponseEntity<AssignmentResponse> createAssignment(
                        @PathVariable Long courseId,
                        @Valid @RequestBody AssignmentCreateRequest request) {
                AssignmentResponse response = assignmentService
                                .createAssignment(courseId, request);

                URI location = URI.create(
                                "/api/courses/" + courseId + "/assignments/" + response.id());

                return ResponseEntity.created(location).body(response);
        }

        @ApiDescription("강의별 과제 목록")
        @GetMapping
        public ResponseEntity<List<AssignmentResponse>> getAssignments(
                        @PathVariable Long courseId) {
                List<AssignmentResponse> responses = assignmentService
                                .getAssignments(courseId);

                return ResponseEntity.ok(responses);
        }

        @ApiDescription("과제 단건 조회")
        @GetMapping("/{assignmentId}")
        public ResponseEntity<AssignmentResponse> getAssignment(
                        @PathVariable Long courseId,
                        @PathVariable Long assignmentId) {
                AssignmentResponse response = assignmentService
                                .getAssignment(courseId, assignmentId);

                return ResponseEntity.ok(response);
        }

        @ApiDescription("과제 삭제")
        @DeleteMapping("/{assignmentId}")
        public ResponseEntity<Void> deleteAssignment(
                        @PathVariable Long courseId,
                        @PathVariable Long assignmentId) {
                assignmentService.deleteAssignment(courseId, assignmentId);

                return ResponseEntity.noContent().build();
        }

    @ApiDescription("과제 상태·결과물 링크 변경")
        @PatchMapping("/{assignmentId}")
        public ResponseEntity<AssignmentResponse> updateAssignment(
                        @PathVariable Long courseId,
                        @PathVariable Long assignmentId,
                        @Valid @RequestBody AssignmentUpdateRequest request) {
                AssignmentResponse response = assignmentService
                                .updateAssignment(courseId, assignmentId, request);

                return ResponseEntity.ok(response);
        }
}
