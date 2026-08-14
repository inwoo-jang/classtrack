package com.inwoo.classtrack.dto.assignment;

import com.inwoo.classtrack.domain.AssignmentMode;
import com.inwoo.classtrack.domain.AssignmentRequirement;
import com.inwoo.classtrack.domain.AssignmentStatus;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 과제의 진행 상태와 결과물 링크를 갱신한다.
 * submissionUrl 이 null 이거나 비어 있으면 링크를 지운 것으로 본다.
 */
public record AssignmentUpdateRequest(
        @NotBlank(message = "과제명은 필수입니다.") String title,

        @Size(max = 5000, message = "과제 설명은 5000자를 넘을 수 없습니다.") String description,

        LocalDateTime dueDate,

        @NotNull(message = "과제 형태는 필수입니다.") AssignmentMode assignmentMode,

        @NotNull(message = "필수 여부는 필수입니다.") AssignmentRequirement requirement,

        @NotNull(message = "상태는 필수입니다.") AssignmentStatus status,

        @Size(max = 10, message = "결과물 링크는 최대 10개까지 등록할 수 있습니다.")
        List<@Size(max = 500, message = "결과물 링크는 500자를 넘을 수 없습니다.") String> submissionUrls) {

    @AssertTrue(message = "과제 설명은 최대 10줄까지 입력할 수 있습니다.")
    public boolean isDescriptionLineCountValid() {
        return description == null || description.split("\\R", -1).length <= 10;
    }
}
