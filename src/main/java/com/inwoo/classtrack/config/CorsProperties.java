package com.inwoo.classtrack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 브라우저에서 이 API 를 호출해도 되는 출처 목록.
 *
 * <p>프론트와 API 가 같은 주소에서 서빙되면(개발 중 Vite 프록시, 통합 배포) 교차 출처가
 * 아예 없으므로 비워둬도 된다. Vercel 처럼 프론트를 따로 배포할 때만 채운다.
 *
 * <p>운영에서는 환경변수 {@code CORS_ALLOWED_ORIGINS} 로 주입한다 (쉼표로 구분).
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        allowedOrigins = allowedOrigins == null
                ? List.of()
                : allowedOrigins.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    public boolean isEnabled() {
        return !allowedOrigins.isEmpty();
    }
}
