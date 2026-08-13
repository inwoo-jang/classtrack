package com.inwoo.classtrack.controller;

import com.inwoo.classtrack.dev.ApiDescription;

import com.inwoo.classtrack.config.DeveloperProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 개발자 정보. local 프로필에만 값이 있으므로 prod 에서는 404 가 된다.
 * 설정 파일만으로 노출 여부가 갈리는 예시이기도 하다.
 */
@RestController
@RequestMapping("/api/developer")
public class DeveloperInfoController {

    private final DeveloperProperties developer;
    private final Environment environment;

    public DeveloperInfoController(
            DeveloperProperties developer, Environment environment) {
        this.developer = developer;
        this.environment = environment;
    }

    @ApiDescription("개발자 정보 (local 전용)")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDeveloper() {
        if (developer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 프로필을 명시하지 않고 띄우면 active 는 비어 있고 default 만 적용된다
        String[] active = environment.getActiveProfiles();
        List<String> profiles = List.of(
                active.length > 0 ? active : environment.getDefaultProfiles());

        return ResponseEntity.ok(Map.of(
                "name", developer.name(),
                "email", nullToEmpty(developer.email()),
                "role", nullToEmpty(developer.role()),
                "organization", nullToEmpty(developer.organization()),
                "github", nullToEmpty(developer.github()),
                "activeProfiles", profiles));
    }

    /** Map.of 는 null 값을 허용하지 않으므로 빈 문자열로 바꿔 담는다. */
    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
