package org.example.chaenggim.global.config;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** 테스트 고정 Clock: 2026-11-09T09:00 Asia/Seoul (월요일). 09 문서 1절. */
@TestConfiguration
public class TestClockConfig {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    public static final ZonedDateTime NOW = ZonedDateTime.of(2026, 11, 9, 9, 0, 0, 0, ZONE);

    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(NOW.toInstant(), ZONE);
    }
}
