package com.parkjonghwan.meal_auto.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 서비스의 "오늘"은 항상 한국 날짜
// 서버(Render 등)의 시간대가 UTC여도 한국 오전 9시 이전 기록이 전날로 저장되지 않도록
// 날짜가 필요한 곳은 LocalDate.now() 대신 LocalDate.now(clock)을 씀 (테스트에서는 시각을 고정할 수 있음)
@Configuration
public class TimeConfig {
  public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

  @Bean
  public Clock clock() {
    return Clock.system(ZONE);
  }
}
