package com.inwoo.classtrack.dev;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 구현 현황 화면용. 내부 구조를 그대로 드러내므로 local 프로필에서만 등록된다.
 */
@Profile("local")
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
