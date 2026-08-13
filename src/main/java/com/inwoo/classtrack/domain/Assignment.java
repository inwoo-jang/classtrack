package com.inwoo.classtrack.domain;

import jakarta.persistence.Column;
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

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.TODO;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /** 과제 결과물 위치 (Google Drive, GitHub 저장소 등). 없을 수 있다. */
    @Column(name = "submission_url", length = 500)
    private String submissionUrl;

    /** 링크가 실제로 열리는지. 저장 직후에는 PENDING 이고 비동기 확인 후 갱신된다. */
    @Enumerated(EnumType.STRING)
    @Column(name = "link_status", nullable = false, columnDefinition = "varchar(20) default 'NONE'")
    private LinkStatus linkStatus = LinkStatus.NONE;

    @Column(name = "link_checked_at")
    private LocalDateTime linkCheckedAt;

    protected Assignment() {
    }

    public Assignment(
            Course course,
            String title,
            String description,
            LocalDateTime dueDate,
            String submissionUrl) {
        this.course = course;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.submissionUrl = normalizeUrl(submissionUrl);
        this.linkStatus = this.submissionUrl == null ? LinkStatus.NONE : LinkStatus.PENDING;
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

    public String getSubmissionUrl() {
        return submissionUrl;
    }

    public LinkStatus getLinkStatus() {
        return linkStatus;
    }

    public LocalDateTime getLinkCheckedAt() {
        return linkCheckedAt;
    }

    /**
     * 결과물 링크를 교체한다. 빈 문자열은 "링크 없음"으로 취급한다.
     * 링크가 실제로 바뀐 경우에만 확인 상태를 초기화한다.
     */
    public void linkSubmission(String submissionUrl) {
        String next = normalizeUrl(submissionUrl);
        if (java.util.Objects.equals(this.submissionUrl, next)) {
            return;
        }

        this.submissionUrl = next;
        this.linkStatus = next == null ? LinkStatus.NONE : LinkStatus.PENDING;
        this.linkCheckedAt = null;
    }

    /** 비동기 검증 결과를 기록한다. */
    public void recordLinkCheck(LinkStatus result) {
        this.linkStatus = result;
        this.linkCheckedAt = LocalDateTime.now();
    }

    private static String normalizeUrl(String url) {
        return (url == null || url.isBlank()) ? null : url.strip();
    }

    public void update(
            String title,
            String description,
            LocalDateTime dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
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
