package com.inwoo.classtrack.controller;

import com.inwoo.classtrack.dev.ApiDescription;

import com.inwoo.classtrack.dto.assignment.AssignmentResponse;
import com.inwoo.classtrack.service.AssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 강의에 종속되지 않은 과제 조회.
 * 특정 강의 하위의 과제는 {@link AssignmentController} 가 담당한다.
 */
@RestController
@RequestMapping("/api/assignments")
public class AssignmentQueryController {

    private final AssignmentService assignmentService;

    public AssignmentQueryController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @ApiDescription("전체 과제 (마감일 순)")
    @GetMapping
    public ResponseEntity<List<AssignmentResponse>> getAllAssignments() {
        return ResponseEntity.ok(assignmentService.getAllAssignments());
    }
}
