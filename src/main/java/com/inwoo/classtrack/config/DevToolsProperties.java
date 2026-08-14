package com.inwoo.classtrack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 개발 도구(/dev 화면 = 구현 현황 + 로그) 노출 여부.
 *
 * <p>프로필로 나누지 않고 하나의 설정으로 묶은 이유: 이전에는 구현 현황만
 * {@code @Profile("local")} 로 막고 로그는 열려 있었다. 둘의 노출 범위가 어긋나면
 * 어느 쪽이 열려 있는지 코드를 읽어야 알 수 있다.
 *
 * <p>로그에는 스택트레이스와 요청 인자가 담기므로 기본값은 <b>꺼짐</b>이다.
 * 포트폴리오로 보여주고 싶으면 운영에서도 {@code DEV_TOOLS_ENABLED=true} 로 켤 수 있다.
 */
@ConfigurationProperties(prefix = "app.dev")
public record DevToolsProperties(boolean enabled) {
}
