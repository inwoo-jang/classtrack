package com.inwoo.classtrack.repository;

import com.inwoo.classtrack.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    /** 이미 쓰인 과목명. 설정에 없는 값도 목록에 다시 나타나게 하려고 읽는다. */
    @Query("select distinct c.subject from Course c order by c.subject")
    List<String> findDistinctSubjects();
}
