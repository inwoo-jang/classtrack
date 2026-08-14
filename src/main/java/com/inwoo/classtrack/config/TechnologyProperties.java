package com.inwoo.classtrack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 기술 추천 목록의 기본값. {@code app.technologies} 를 받는다.
 *
 * <p>여기 없는 기술도 자유롭게 입력할 수 있다. 한 번 쓰인 값은 DB 에서 읽어
 * 자동으로 목록에 합쳐지므로, 설정은 "처음에 보여줄 것"만 담으면 된다.
 */
@ConfigurationProperties(prefix = "app")
public record TechnologyProperties(List<String> technologies) {

    public TechnologyProperties {
        technologies = technologies == null ? List.of() : List.copyOf(technologies);
    }
}
