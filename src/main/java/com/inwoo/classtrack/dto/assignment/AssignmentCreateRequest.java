package com.inwoo.classtrack.dto.assignment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AssignmentCreateRequest(
        @NotBlank(message = "과제명은 필수입니다.") String title,

        String description,

        @NotNull(message = "마감일은 필수입니다.") LocalDateTime dueDate,

        @Size(max = 500, message = "결과물 링크는 500자를 넘을 수 없습니다.") String submissionUrl) {

}
