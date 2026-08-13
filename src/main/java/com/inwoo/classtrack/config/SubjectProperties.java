package com.inwoo.classtrack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 과목 추천 목록. {@code app.subjects} 를 받는다.
 *
 * <p>enum 으로 두지 않은 이유: 과목이 늘어날 때마다 코드를 고치고 다시 배포해야 한다.
 * 여기 없는 값도 입력할 수 있게 두고, 화면에서는 드롭다운으로 제안만 한다.
 */
@ConfigurationProperties(prefix = "app")
public record SubjectProperties(List<String> subjects) {

    public SubjectProperties {
        subjects = subjects == null ? List.of() : List.copyOf(subjects);
    }
}
