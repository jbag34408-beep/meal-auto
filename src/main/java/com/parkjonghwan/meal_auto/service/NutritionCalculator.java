package com.parkjonghwan.meal_auto.service;

import com.parkjonghwan.meal_auto.domain.MealEntry;
import org.springframework.stereotype.Component;

@Component
public class NutritionCalculator {

  // 100g 기준 값을 실제 섭취량으로 환산
  private double scale(double per100g, double grams) {
    return per100g * (grams / 100);
  }

  public double calculateKcal(MealEntry entry) {
    return scale(entry.getFood().getKcalPer100g(), entry.getGrams());
  }

  public double calculateCarbohydrate(MealEntry entry) {
    return scale(entry.getFood().getCarbohydrate(), entry.getGrams());
  }

  public double calculateProtein(MealEntry entry) {
    return scale(entry.getFood().getProtein(), entry.getGrams());
  }

  public double calculateFat(MealEntry entry) {
    return scale(entry.getFood().getFat(), entry.getGrams());
  }
}
