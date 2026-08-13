package com.inwoo.classtrack.domain;

/** 결과물 링크가 실제로 열리는지. 비동기로 확인한다. */
public enum LinkStatus {
    /** 링크가 없음 */
    NONE,
    /** 링크는 있는데 아직 확인 전 */
    PENDING,
    /** 응답 정상 */
    OK,
    /** 응답 없음 또는 4xx/5xx */
    BROKEN
}
