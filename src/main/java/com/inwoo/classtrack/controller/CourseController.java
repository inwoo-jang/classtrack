package com.inwoo.classtrack.controller;

import com.inwoo.classtrack.dev.ApiDescription;

import com.inwoo.classtrack.aspect.LogExecution;
import com.inwoo.classtrack.dto.course.CourseCreateRequest;
import com.inwoo.classtrack.dto.course.CourseResponse;
import com.inwoo.classtrack.dto.course.CourseUpdateRequest;
import com.inwoo.classtrack.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @ApiDescription("강의 등록")
    @LogExecution
    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CourseCreateRequest request) {
        CourseResponse response = courseService.createCourse(request);

        URI location = URI.create("/api/courses/" + response.id());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @ApiDescription("강의 목록 (과제 집계 포함)")
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getCourses() {
        List<CourseResponse> responses = courseService.getCourses();

        return ResponseEntity.ok(responses);
    }

    @ApiDescription("강의 상세")
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponse> getCourse(
            @PathVariable Long courseId) {
        CourseResponse response = courseService.getCourse(courseId);

        return ResponseEntity.ok(response);
    }

    /** PUT 은 전체 교체. 부분 수정이 아니므로 모든 필드를 받는다. */
    @ApiDescription("강의 수정 (PK 제외 전체)")
    @LogExecution
    @PutMapping("/{courseId}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseUpdateRequest request) {
        CourseResponse response = courseService.updateCourse(courseId, request);

        return ResponseEntity.ok(response);
    }

    @ApiDescription("강의 삭제 (딸린 과제도 함께)")
    @LogExecution
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);

        return ResponseEntity.noContent().build();
    }
}