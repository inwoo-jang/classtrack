package com.inwoo.classtrack.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 교차 출처 허용 설정.
 *
 * <p>브라우저는 다른 출처(프로토콜·호스트·포트 중 하나라도 다름)의 응답을 스크립트에
 * 넘겨주기 전에 서버의 허락을 확인한다. 프론트를 Vercel 에 따로 배포하면
 * {@code classtrack.vercel.app} 과 {@code …railway.app} 이 서로 남남이 되므로 이 설정이 필요하다.
 *
 * <p>목록이 비어 있으면 아무것도 등록하지 않는다 — 같은 출처로 서빙되는 구성에서는
 * 필요 없고, 열어둘 이유도 없다.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    private final CorsProperties cors;

    public CorsConfig(CorsProperties cors) {
        this.cors = cors;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (!cors.isEnabled()) {
            log.info("CORS 미설정 — 같은 출처에서만 호출할 수 있다");
            return;
        }

        log.info("CORS 허용 출처: {}", cors.allowedOrigins());

        registry.addMapping("/api/**")
                // allowedOrigins 가 아니라 Patterns 를 쓴다.
                // Vercel 은 커밋마다 미리보기 도메인을 만들므로 와일드카드가 필요하다.
                .allowedOriginPatterns(cors.allowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // PATCH·DELETE 나 JSON 본문 요청은 브라우저가 OPTIONS 로 먼저 물어본다.
                // 그 결과를 1시간 캐시해 매 요청마다 왕복하지 않게 한다.
                .maxAge(3600);
    }
}
