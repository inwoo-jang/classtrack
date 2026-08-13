package com.inwoo.classtrack.dto.course;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 강의 전체 수정. PK(id)를 제외한 모든 필드를 교체한다.
 *
 * <p>생성 요청과 필드가 같지만 별도 record 로 둔다. "등록할 때만 받는 값"이나
 * "수정은 막을 값"이 생기면 한쪽만 고치면 되기 때문이다.
 */
public record CourseUpdateRequest(

        @NotBlank(message = "강의명은 필수입니다.") String title,

        @NotBlank(message = "과목은 필수입니다.") String subject,

        @NotBlank(message = "강사명은 필수입니다.") String instructor,

        @NotNull(message = "강의 시작일은 필수입니다.") LocalDate startDate,

        @NotNull(message = "수강 기간은 필수입니다.") @Min(value = 1, message = "수강 기간은 1일 이상이어야 합니다.") Integer durationDays,

        @NotBlank(message = "강의 장소는 필수입니다.") String location,

        @NotNull(message = "대면/비대면 여부는 필수입니다.") Boolean liveLecture,

        String practiceProfessor) {

    /** 비대면 강의는 실습을 맡는 교수가 따로 있으므로 이름을 반드시 받는다. */
    @AssertTrue(message = "비대면 강의는 실습교수 이름이 필수입니다.")
    public boolean isPracticeProfessorValid() {
        return Boolean.TRUE.equals(liveLecture)
                || (practiceProfessor != null && !practiceProfessor.isBlank());
    }
}
