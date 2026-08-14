package com.inwoo.classtrack.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String instructor;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(nullable = false)
    private String location;

    @Column(name = "live_lecture", nullable = false, columnDefinition = "boolean default true")
    private boolean liveLecture = true;

    @Column(name = "practice_professor")
    private String practiceProfessor;

    /**
     * 이 강의에서 다룬 기술. 과제의 technologies 와 의미가 다르다 —
     * 여기는 "배운 것", 과제는 "직접 쓴 것"이다.
     *
     * <p>별도 테이블로 정규화하지 않은 이유: 기술명으로 검색할 일이 아직 없고,
     * 표기 흔들림은 추천 목록 API 로 줄인다.
     */
    @ElementCollection
    @CollectionTable(name = "course_technologies", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "technology", length = 60)
    private List<String> technologies = new ArrayList<>();

    protected Course() {
    }

    public Course(
            String title,
            String subject,
            String instructor,
            LocalDate startDate,
            Integer durationDays,
            String location,
            boolean liveLecture,
            String practiceProfessor,
            List<String> technologies) {
        this.title = title;
        this.subject = subject;
        this.instructor = instructor;
        this.startDate = startDate;
        this.durationDays = durationDays;
        this.location = location;
        this.liveLecture = liveLecture;
        this.practiceProfessor = liveLecture ? null : practiceProfessor;
        replaceTechnologies(technologies);
    }

    // Setters 대신 update 메서드로만 상태를 바꾼다.

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public String getInstructor() {
        return instructor;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public String getLocation() {
        return location;
    }

    public boolean isLiveLecture() {
        return liveLecture;
    }

    public String getPracticeProfessor() {
        return practiceProfessor;
    }

    public List<String> getTechnologies() {
        return technologies;
    }

    /** 컬렉션은 통째로 갈아끼우지 않고 내용만 바꾼다 (JPA 가 추적하는 인스턴스를 유지). */
    private void replaceTechnologies(List<String> values) {
        this.technologies.clear();
        if (values == null) {
            return;
        }
        values.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(String::strip)
                .distinct()
                .forEach(this.technologies::add);
    }

    // 종료일과 진행 상태는 주말·공휴일을 빼고 계산해야 하므로 엔티티가 알 수 없다.
    // com.inwoo.classtrack.calendar.AcademicCalendar 가 담당한다.

    public void update(
            String title,
            String subject,
            String instructor,
            LocalDate startDate,
            Integer durationDays,
            String location,
            boolean liveLecture,
            String practiceProfessor,
            List<String> technologies) {
        this.title = title;
        this.subject = subject;
        this.instructor = instructor;
        this.startDate = startDate;
        this.durationDays = durationDays;
        this.location = location;
        this.liveLecture = liveLecture;
        this.practiceProfessor = liveLecture ? null : practiceProfessor;
        replaceTechnologies(technologies);
    }

}
