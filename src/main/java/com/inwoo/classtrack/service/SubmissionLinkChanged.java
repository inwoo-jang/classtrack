package com.inwoo.classtrack.service;

/**
 * 과제의 결과물 링크가 새로 지정되었을 때 발행하는 이벤트.
 *
 * <p>엔티티가 아니라 <b>id 와 url 만</b> 담는다. 엔티티를 넘기면 비동기 스레드에서
 * 영속성 컨텍스트가 없어 지연 로딩이 터진다.
 */
public record SubmissionLinkChanged(Long assignmentId, String url) {
}
