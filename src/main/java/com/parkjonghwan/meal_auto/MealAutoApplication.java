package com.parkjonghwan.meal_auto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MealAutoApplication {

  public static void main(String[] args) {
    SpringApplication.run(MealAutoApplication.class, args);
  }
}
