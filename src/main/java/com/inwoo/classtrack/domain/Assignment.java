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

    protected Assignment() {
    }

    public Assignment(
            Course course,
            String title,
            String description,
            LocalDateTime dueDate,
            AssignmentMode assignmentMode,
            AssignmentRequirement requirement,
            List<String> submissionUrls) {
        this.course = course;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.assignmentMode = assignmentMode;
        this.requirement = requirement;
        replaceSubmissionLinks(submissionUrls);
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
            List<String> submissionUrls) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.assignmentMode = assignmentMode;
        this.requirement = requirement;
        replaceSubmissionLinks(submissionUrls);
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
