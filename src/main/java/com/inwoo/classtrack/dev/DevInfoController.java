package com.inwoo.classtrack.dev;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 구현 현황 화면용. 내부 구조를 드러내므로 app.dev.enabled 가 true 일 때만 등록된다.
 */
@ConditionalOnProperty(prefix = "app.dev", name = "enabled", havingValue = "true")
@RestController
@RequestMapping("/api/dev")
public class DevInfoController {

    private final DevInfoService devInfoService;

    public DevInfoController(DevInfoService devInfoService) {
        this.devInfoService = devInfoService;
    }

    @GetMapping("/overview")
    @ApiDescription("엔드포인트·서비스·엔티티 구조 조회 (개발용)")
    public ResponseEntity<DevOverview> getOverview() {
        return ResponseEntity.ok(devInfoService.build());
    }
}
