package com.parkjonghwan.meal_auto;

import com.parkjonghwan.meal_auto.config.DatabaseUrl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MealAutoApplication {

  public static void main(String[] args) {
    // Render PostgreSQL·Neon의 postgres://… 주소를 그대로 DB_URL에 넣어도 되도록 JDBC 형식으로 바꿈
    DatabaseUrl.applyFromEnvironment(System.getenv());
    SpringApplication.run(MealAutoApplication.class, args);
  }
}
