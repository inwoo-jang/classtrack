package com.inwoo.classtrack.service;

import com.inwoo.classtrack.domain.Assignment;
import com.inwoo.classtrack.domain.LinkStatus;
import com.inwoo.classtrack.repository.AssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 링크 확인 결과를 저장한다.
 *
 * <p>{@link LinkCheckService} 안에 두지 않고 Bean 을 나눈 이유: 같은 객체 안에서
 * {@code this.record(...)} 로 부르면 프록시를 거치지 않아 {@code @Transactional} 이
 * 무시된다. 트랜잭션도 @Async 와 같은 프록시 기반이라 self-invocation 에 걸린다.
 *
 * <p>HTTP 호출은 이 밖에서 끝낸 뒤 결과만 넘긴다. 외부 응답을 기다리는 동안
 * DB 커넥션을 붙잡고 있으면 커넥션 풀이 마른다.
 */
@Service
public class LinkCheckRecorder {

    private final AssignmentRepository assignmentRepository;

    public LinkCheckRecorder(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    /**
     * @param checkedUrl 확인 당시의 URL. 확인하는 동안 사용자가 링크를 또 바꿨다면
     *                   낡은 결과이므로 버린다.
     */
    @Transactional
    public void record(Long assignmentId, String checkedUrl, LinkStatus result) {
        Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if (assignment == null || !checkedUrl.equals(assignment.getSubmissionUrl())) {
            return;
        }
        assignment.recordLinkCheck(result);
    }
}
