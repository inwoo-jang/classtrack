package com.inwoo.classtrack.repository;

import com.inwoo.classtrack.domain.Assignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository
        extends JpaRepository<Assignment, Long> {

    List<Assignment> findAllByCourseIdOrderByDueDateAsc(Long courseId);

    Optional<Assignment> findByIdAndCourseId(Long assignmentId, Long courseId);

    /**
     * 전체 과제를 마감일 순으로. 응답에 강의명이 들어가므로 course 를 함께 로딩해
     * N+1 조회를 피한다.
     */
    @EntityGraph(attributePaths = "course")
    List<Assignment> findAllByOrderByDueDateAsc();

    /** 강의 목록 화면의 집계용. 강의마다 따로 조회하지 않도록 한 번에 가져온다. */
    @EntityGraph(attributePaths = "course")
    List<Assignment> findAllByCourseIdIn(List<Long> courseIds);

    /** 강의를 지우기 전에 딸린 과제를 먼저 정리할 때 쓴다. */
    void deleteAllByCourseId(Long courseId);

    long countByCourseId(Long courseId);
}
