package com.parkjonghwan.meal_auto.dto;

import java.time.LocalDate;

public record DailySummary(LocalDate date, int mealCount, double totalKcal,
                           double totalCarbohydrate, double totalProtein, double totalFat) {

  // 서버 안에서 쓰는 지름길 (JSON에는 안 나감)
  public Nutrition total() {
    return new Nutrition(totalKcal, totalCarbohydrate, totalProtein, totalFat);
  }
}
