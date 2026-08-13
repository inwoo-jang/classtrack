package com.inwoo.classtrack.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

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
            String practiceProfessor) {
        this.title = title;
        this.subject = subject;
        this.instructor = instructor;
        this.startDate = startDate;
        this.durationDays = durationDays;
        this.location = location;
        this.liveLecture = liveLecture;
        this.practiceProfessor = liveLecture ? null : practiceProfessor;
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
            String practiceProfessor) {
        this.title = title;
        this.subject = subject;
        this.instructor = instructor;
        this.startDate = startDate;
        this.durationDays = durationDays;
        this.location = location;
        this.liveLecture = liveLecture;
        this.practiceProfessor = liveLecture ? null : practiceProfessor;
    }

}
