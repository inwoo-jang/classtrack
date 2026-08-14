package com.inwoo.classtrack.controller;

import com.inwoo.classtrack.config.TechnologyProperties;
import com.inwoo.classtrack.dev.ApiDescription;
import com.inwoo.classtrack.repository.AssignmentRepository;
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
 * 기술 추천 목록. 과목({@link SubjectController})과 같은 방식이다.
 *
 * <p>별도 테이블로 정규화하지 않은 이유: 기술명으로 검색할 일이 아직 없다.
 * 표기가 흔들리는 문제("Spring Boot" vs "SpringBoot")만 막으면 되는데,
 * 그건 이미 쓰인 값을 모아 제안하는 것으로 충분하다.
 */
@RestController
@RequestMapping("/api/technologies")
public class TechnologyController {

    private final TechnologyProperties properties;
    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;

    public TechnologyController(
            TechnologyProperties properties,
            CourseRepository courseRepository,
            AssignmentRepository assignmentRepository) {
        this.properties = properties;
        this.courseRepository = courseRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @ApiDescription("기술 추천 목록 (설정 + 사용 중인 값)")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<String>> getTechnologies() {
        // LinkedHashSet — 설정 순서를 유지하면서 중복만 걸러낸다
        Set<String> merged = new LinkedHashSet<>(properties.technologies());
        merged.addAll(courseRepository.findDistinctTechnologies());
        merged.addAll(assignmentRepository.findDistinctTechnologies());

        return ResponseEntity.ok(List.copyOf(merged));
    }
}
