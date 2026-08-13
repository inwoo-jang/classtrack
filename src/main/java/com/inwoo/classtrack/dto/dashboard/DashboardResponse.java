package com.inwoo.classtrack.dto.dashboard;

import com.inwoo.classtrack.dto.assignment.AssignmentResponse;
import com.inwoo.classtrack.dto.course.AssignmentSummary;
import com.inwoo.classtrack.dto.course.CourseResponse;

import java.util.List;

/**
 * 첫 화면에 필요한 것을 한 번의 요청으로 모아 준다.
 * (강의 / 과제를 따로 호출하면 화면이 두 번 흔들린다.)
 */
public record DashboardResponse(
        CourseStats courses,
        AssignmentSummary assignments,
        /** 오늘 기준 진행 중인 강의 */
        List<CourseResponse> ongoingCourses,
        /** 진행 중인 과제, 마감이 급한 순 */
        List<AssignmentResponse> inProgressAssignments) {
}
