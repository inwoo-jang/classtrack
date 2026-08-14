package com.inwoo.classtrack.controller;

import com.inwoo.classtrack.dev.ApiDescription;
import com.inwoo.classtrack.portfolio.PortfolioMarkdownService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioMarkdownService markdownService;

    public PortfolioController(PortfolioMarkdownService markdownService) {
        this.markdownService = markdownService;
    }

    /**
     * 학습 기록을 Markdown 으로.
     *
     * <p>파일 다운로드(Content-Disposition)로 주지 않는다. 프론트가 다른 출처(Vercel)에 있어
     * 브라우저가 바로 저장하게 하려면 Blob 처리가 필요한데, 실제로는 README 에 붙여넣는 일이
     * 더 많다. 본문만 주고 복사·저장은 화면에서 처리한다.
     *
     * @param featuredOnly 대표 과제만. 과제가 남지 않는 강의는 통째로 빠진다
     */
    @ApiDescription("학습 기록 Markdown 생성")
    @GetMapping(value = "/markdown", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public ResponseEntity<String> markdown(
            @RequestParam(defaultValue = "false") boolean featuredOnly) {
        String markdown = markdownService.render(featuredOnly);

        return ResponseEntity.ok()
                .contentLength(markdown.getBytes(StandardCharsets.UTF_8).length)
                .body(markdown);
    }
}
