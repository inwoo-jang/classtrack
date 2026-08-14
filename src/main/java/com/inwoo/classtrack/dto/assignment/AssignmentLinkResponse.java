package com.inwoo.classtrack.dto.assignment;

import com.inwoo.classtrack.domain.AssignmentLink;
import com.inwoo.classtrack.domain.LinkStatus;

import java.time.LocalDateTime;

public record AssignmentLinkResponse(
        String url,
        LinkStatus status,
        LocalDateTime checkedAt) {

    public static AssignmentLinkResponse from(AssignmentLink link) {
        return new AssignmentLinkResponse(link.getUrl(), link.getStatus(), link.getCheckedAt());
    }
}
