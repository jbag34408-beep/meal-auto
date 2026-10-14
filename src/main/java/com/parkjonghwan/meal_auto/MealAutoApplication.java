package com.parkjonghwan.meal_auto;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MealAutoApplication {

  public static void main(String[] args) {
    SpringApplication.run(MealAutoApplication.class, args);
  }

  // "오늘"을 한국 시간 기준으로 (테스트에서는 고정된 Clock으로 바꿔 끼움)
  @Bean
  public Clock clock() {
    return Clock.system(ZoneId.of("Asia/Seoul"));
  }
}
