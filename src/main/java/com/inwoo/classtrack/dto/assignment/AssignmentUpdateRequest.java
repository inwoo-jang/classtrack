package com.inwoo.classtrack.dto.assignment;

import com.inwoo.classtrack.domain.AssignmentStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 과제의 진행 상태와 결과물 링크를 갱신한다.
 * submissionUrl 이 null 이거나 비어 있으면 링크를 지운 것으로 본다.
 */
public record AssignmentUpdateRequest(
        @NotNull(message = "상태는 필수입니다.") AssignmentStatus status,

        @Size(max = 500, message = "결과물 링크는 500자를 넘을 수 없습니다.") String submissionUrl) {

}
