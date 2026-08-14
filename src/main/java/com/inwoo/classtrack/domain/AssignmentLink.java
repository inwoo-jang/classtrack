package com.inwoo.classtrack.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

@Embeddable
public class AssignmentLink {

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LinkStatus status = LinkStatus.PENDING;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    protected AssignmentLink() {
    }

    public AssignmentLink(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public LinkStatus getStatus() {
        return status;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void recordCheck(LinkStatus result) {
        this.status = result;
        this.checkedAt = LocalDateTime.now();
    }
}
