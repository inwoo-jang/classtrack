package com.inwoo.classtrack.dto.assignment;

import com.inwoo.classtrack.domain.AssignmentMode;
import com.inwoo.classtrack.domain.AssignmentRequirement;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record AssignmentCreateRequest(
        @NotBlank(message = "과제명은 필수입니다.") String title,

        @Size(max = 5000, message = "과제 설명은 5000자를 넘을 수 없습니다.") String description,

        LocalDateTime dueDate,

        @NotNull(message = "과제 형태는 필수입니다.") AssignmentMode assignmentMode,

        @NotNull(message = "필수 여부는 필수입니다.") AssignmentRequirement requirement,

        @Size(max = 10, message = "결과물 링크는 최대 10개까지 등록할 수 있습니다.")
        List<@Size(max = 500, message = "결과물 링크는 500자를 넘을 수 없습니다.") String> submissionUrls,

        @Size(max = 30, message = "기술은 최대 30개까지 등록할 수 있습니다.")
        List<@Size(max = 60, message = "기술명은 60자를 넘을 수 없습니다.") String> technologies,

        /** 포트폴리오에 내보낼 대표 과제 */
        boolean featured,

        /** 팀 과제일 때 팀 인원. 개인 과제면 무시된다 */
        @Min(value = 2, message = "팀 규모는 2명 이상이어야 합니다.") Integer teamSize) {

    @AssertTrue(message = "과제 설명은 최대 10줄까지 입력할 수 있습니다.")
    public boolean isDescriptionLineCountValid() {
        return description == null || description.split("\\R", -1).length <= 10;
    }
}
