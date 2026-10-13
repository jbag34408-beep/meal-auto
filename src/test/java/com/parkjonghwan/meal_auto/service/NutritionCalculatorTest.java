package com.parkjonghwan.meal_auto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.MealType;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class NutritionCalculatorTest {

  private final NutritionCalculator calculator = new NutritionCalculator();
  private final Food rice = new Food("쌀밥", 200, 45, 4, 0.5);

  @Test
  void 섭취량_150g이면_100g_기준의_1점5배() {
    // given
    MealEntry entry = new MealEntry(LocalDate.now(), MealType.LUNCH, rice, 150);

    // when & then
    assertEquals(300.0, calculator.calculateKcal(entry), 0.001);
    assertEquals(67.5, calculator.calculateCarbohydrate(entry), 0.001);
    assertEquals(6.0, calculator.calculateProtein(entry), 0.001);
    assertEquals(0.75, calculator.calculateFat(entry), 0.001);
  }

  @Test
  void 섭취량_0g이면_0() {
    MealEntry entry = new MealEntry(LocalDate.now(), MealType.SNACK, rice, 0);
    assertEquals(0.0, calculator.calculateKcal(entry), 0.001);
  }
}