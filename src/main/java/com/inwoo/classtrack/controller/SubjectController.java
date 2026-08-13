package com.inwoo.classtrack.controller;

import com.inwoo.classtrack.config.SubjectProperties;
import com.inwoo.classtrack.dev.ApiDescription;
import com.inwoo.classtrack.repository.CourseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 강의 등록·수정 폼의 과목 추천 목록.
 *
 * <p>설정에 적어둔 기본 8개를 먼저 두고, DB 에 이미 쓰인 과목명을 뒤에 붙인다.
 * 한 번 직접 입력한 과목은 다음부터 드롭다운에 나온다.
 */
@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectProperties properties;
    private final CourseRepository courseRepository;

    public SubjectController(
            SubjectProperties properties, CourseRepository courseRepository) {
        this.properties = properties;
        this.courseRepository = courseRepository;
    }

    @ApiDescription("과목 추천 목록 (설정 + 사용 중인 값)")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<String>> getSubjects() {
        // LinkedHashSet — 설정 순서를 유지하면서 중복만 걸러낸다
        Set<String> merged = new LinkedHashSet<>(properties.subjects());
        merged.addAll(courseRepository.findDistinctSubjects());

        return ResponseEntity.ok(List.copyOf(merged));
    }
}
