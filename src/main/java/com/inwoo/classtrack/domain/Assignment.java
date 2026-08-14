package com.inwoo.classtrack.domain;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_mode", nullable = false, length = 20, columnDefinition = "varchar(20) default 'INDIVIDUAL'")
    private AssignmentMode assignmentMode = AssignmentMode.INDIVIDUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "requirement", nullable = false, length = 20, columnDefinition = "varchar(20) default 'REQUIRED'")
    private AssignmentRequirement requirement = AssignmentRequirement.REQUIRED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.TODO;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @ElementCollection
    @CollectionTable(name = "assignment_links", joinColumns = @JoinColumn(name = "assignment_id"))
    private List<AssignmentLink> submissionLinks = new ArrayList<>();

    /** 이 과제에서 <b>직접 사용한</b> 기술. 강의의 technologies(배운 것)와 구분된다. */
    @ElementCollection
    @CollectionTable(name = "assignment_technologies",
            joinColumns = @JoinColumn(name = "assignment_id"))
    @Column(name = "technology", length = 60)
    private List<String> technologies = new ArrayList<>();

    /**
     * 팀 과제일 때 몇 명이었는지. 개인 과제면 null 이다.
     * "4명 중 백엔드 담당"이 "백엔드 담당"보다 구체적이라 숫자만 받는다
     * (다른 팀원의 역할까지는 기록하지 않는다).
     */
    @Column(name = "team_size")
    private Integer teamSize;

    /** 포트폴리오에 내보낼 대표 과제인지. 전부가 아니라 골라 보여주기 위한 표시. */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean featured = false;

    protected Assignment() {
    }

    public Assignment(
            Course course,
            String title,
            String description,
            LocalDateTime dueDate,
            AssignmentMode assignmentMode,
            AssignmentRequirement requirement,
            List<String> submissionUrls,
            List<String> technologies,
            boolean featured,
            Integer teamSize) {
        this.course = course;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.assignmentMode = assignmentMode;
        this.requirement = requirement;
        replaceSubmissionLinks(submissionUrls);
        replaceTechnologies(technologies);
        this.featured = featured;
        this.teamSize = normalizeTeamSize(assignmentMode, teamSize);
    }

    // Getters는 그대로 유지, Setters는 제거하고 update 메서드와 changeStatus 메서드를 추가

    public Long getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public AssignmentMode getAssignmentMode() {
        return assignmentMode;
    }

    public AssignmentRequirement getRequirement() {
        return requirement;
    }

    public List<AssignmentLink> getSubmissionLinks() {
        return List.copyOf(submissionLinks);
    }

    /**
     * 결과물 링크를 교체한다. 빈 문자열은 "링크 없음"으로 취급한다.
     * 링크가 실제로 바뀐 경우에만 확인 상태를 초기화한다.
     */
    public void replaceSubmissionLinks(List<String> urls) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (urls != null) {
            urls.stream()
                    .map(Assignment::normalizeUrl)
                    .filter(java.util.Objects::nonNull)
                    .forEach(normalized::add);
        }

        submissionLinks.removeIf(link -> !normalized.contains(link.getUrl()));
        for (String url : normalized) {
            boolean exists = submissionLinks.stream().anyMatch(link -> link.getUrl().equals(url));
            if (!exists) submissionLinks.add(new AssignmentLink(url));
        }
    }

    /** 비동기 검증 결과를 기록한다. */
    public void recordLinkCheck(String checkedUrl, LinkStatus result) {
        submissionLinks.stream()
                .filter(link -> link.getUrl().equals(checkedUrl))
                .findFirst()
                .ifPresent(link -> link.recordCheck(result));
    }

    private static String normalizeUrl(String url) {
        return (url == null || url.isBlank()) ? null : url.strip();
    }

    public void update(
            String title,
            String description,
            LocalDateTime dueDate,
            AssignmentMode assignmentMode,
            AssignmentRequirement requirement,
            List<String> submissionUrls,
            List<String> technologies,
            boolean featured,
            Integer teamSize) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.assignmentMode = assignmentMode;
        this.requirement = requirement;
        replaceSubmissionLinks(submissionUrls);
        replaceTechnologies(technologies);
        this.featured = featured;
        this.teamSize = normalizeTeamSize(assignmentMode, teamSize);
    }

    public List<String> getTechnologies() {
        return technologies;
    }

    public boolean isFeatured() {
        return featured;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    /** 개인 과제로 바꾸면 팀 규모는 의미가 없으므로 지운다. (실습교수와 같은 규칙) */
    private static Integer normalizeTeamSize(AssignmentMode mode, Integer teamSize) {
        return mode == AssignmentMode.TEAM ? teamSize : null;
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

    public void changeStatus(AssignmentStatus status) {
        this.status = status;

        // 완료 = 제출로 본다. 완료로 바뀐 순간을 기록하고, 되돌리면 지운다.
        if (status == AssignmentStatus.COMPLETED) {
            this.submittedAt = LocalDateTime.now();
        } else {
            this.submittedAt = null;
        }
    }

}
