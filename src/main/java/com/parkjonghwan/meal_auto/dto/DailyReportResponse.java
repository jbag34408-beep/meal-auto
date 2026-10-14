package com.parkjonghwan.meal_auto.dto;

import com.parkjonghwan.meal_auto.domain.DailyReport;
import java.time.LocalDate;

// 저장된 하루 리포트를 화면에 보낼 때 사용 (회원 정보는 포함하지 않음)
public record DailyReportResponse(
    LocalDate date, int mealCount, double totalKcal, double totalCarbohydrate,
    double totalProtein, double totalFat, String recommendedFood, Double recommendedGrams) {

  public static DailyReportResponse from(DailyReport r) {
    String foodName = r.getRecommendedFood() == null ? null : r.getRecommendedFood().getName();
    return new DailyReportResponse(r.getDate(), r.getMealCount(), r.getTotalKcal(),
        r.getTotalCarbohydrate(), r.getTotalProtein(), r.getTotalFat(),
        foodName, r.getRecommendedGrams());
  }
}
