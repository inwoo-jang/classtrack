package com.inwoo.classtrack.calendar;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDate;
import java.util.List;

/**
 * 휴일 목록. application.yaml 의 {@code app.calendar.holidays} 를 받는다.
 *
 * <p>DB 테이블로 두지 않는 이유: 자주 바뀌지 않고, 바뀌면 배포와 함께 나가는 편이
 * 추적하기 쉽다. 운영자가 화면에서 직접 고쳐야 할 일이 생기면 그때 엔티티로 옮기면 된다.
 */
@ConfigurationProperties(prefix = "app.calendar")
public record AcademicCalendarProperties(List<LocalDate> holidays) {

    public AcademicCalendarProperties {
        holidays = holidays == null ? List.of() : List.copyOf(holidays);
    }
}
