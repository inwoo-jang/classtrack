package com.inwoo.classtrack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application-{profile}.yaml 의 {@code app.developer.*} 를 그대로 받는다.
 *
 * <p>local 프로필에만 정의되어 있으므로 prod 로 띄우면 모든 필드가 null 이다.
 * 즉 "개발 환경에서만 노출되는 정보"가 설정 파일 하나로 갈린다.
 */
@ConfigurationProperties(prefix = "app.developer")
public record DeveloperProperties(
        String name,
        String email,
        String role,
        String organization,
        String github) {

    /** 프로필에 정보가 없으면(=prod) 비어 있는 것으로 본다. */
    public boolean isEmpty() {
        return name == null || name.isBlank();
    }
}
