package com.inwoo.classtrack.service;

import com.inwoo.classtrack.domain.LinkStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 결과물 링크가 실제로 열리는지 확인한다.
 *
 * <p>외부 서버 응답을 기다려야 해서 수 초가 걸릴 수 있다. 사용자는 그 결과를 즉시
 * 알 필요가 없으므로 저장은 바로 끝내고 확인만 뒤로 미룬다.
 */
@Service
public class LinkCheckService {

    private static final Logger log = LoggerFactory.getLogger(LinkCheckService.class);

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final LinkCheckRecorder recorder;
    private final HttpClient httpClient;

    public LinkCheckService(LinkCheckRecorder recorder) {
        this.recorder = recorder;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                // 리다이렉트를 따라가지 않으면 정상 링크도 3xx 로 보여 BROKEN 이 된다
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * @TransactionalEventListener(AFTER_COMMIT) 인 이유:
     * 그냥 @Async 로 부르면 저장 트랜잭션이 커밋되기 <b>전에</b> 비동기 스레드가 출발할 수 있다.
     * 그러면 방금 만든 과제를 조회했을 때 아직 없어서 실패한다.
     *
     * <p>@Async 를 함께 붙여 커밋 후 별도 스레드에서 실행한다.
     */
    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLinkChanged(SubmissionLinkChanged event) {
        LinkStatus result = probe(event.url());
        log.info("링크 확인: assignmentId={}, url={}, 결과={}",
                event.assignmentId(), event.url(), result);

        recorder.record(event.assignmentId(), event.url(), result);
    }

    /** HEAD 로 먼저 찔러보고, 서버가 HEAD 를 막으면 GET 으로 다시 시도한다. */
    private LinkStatus probe(String url) {
        try {
            URI uri = URI.create(url);

            int status = send(uri, "HEAD");
            if (status == 405 || status == 501) {
                status = send(uri, "GET");
            }
            return status >= 200 && status < 400 ? LinkStatus.OK : LinkStatus.BROKEN;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return LinkStatus.BROKEN;
        } catch (Exception e) {
            // 잘못된 URL, DNS 실패, 타임아웃 — 사용자 입장에서는 모두 "안 열린다"
            log.info("링크 확인 실패: url={}, 원인={}", url, e.toString());
            return LinkStatus.BROKEN;
        }
    }

    private int send(URI uri, String method) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .method(method, HttpRequest.BodyPublishers.noBody())
                .timeout(TIMEOUT)
                .header("User-Agent", "ClassTrack-LinkCheck")
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }
}
