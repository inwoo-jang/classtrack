package com.inwoo.classtrack.domain;

/**
 * 강의 진행 상태. 저장하지 않고 시작일과 수강 기간으로 매번 계산한다.
 * (날짜가 흐르면 저장된 값은 곧 거짓이 되므로 컬럼으로 두지 않는다.)
 */
public enum CourseStatus {
    /** 아직 시작 전 */
    UPCOMING,
    /** 오늘이 수강 기간 안 */
    ONGOING,
    /** 수강 기간이 끝남 */
    FINISHED
}
