package com.parkjonghwan.meal_auto.dto;

import com.parkjonghwan.meal_auto.domain.Food;

// 칼로리·탄수화물·단백질·지방 묶음 (식단 추천의 목표·합계에 씀)
public record Nutrition(double kcal, double carbohydrate, double protein, double fat) {
  public static final Nutrition ZERO = new Nutrition(0, 0, 0, 0);

  public static Nutrition of(Food food, double grams) {
    double r = grams / 100;
    return new Nutrition(food.getKcalPer100g() * r, food.getCarbohydrate() * r,
        food.getProtein() * r, food.getFat() * r);
  }

  public Nutrition plus(Nutrition o) {
    return new Nutrition(kcal + o.kcal, carbohydrate + o.carbohydrate, protein + o.protein, fat + o.fat);
  }

  // 뺀 값이 음수면 0 (이미 다 채운 영양소)
  public Nutrition minusFloorZero(Nutrition o) {
    return new Nutrition(Math.max(0, kcal - o.kcal), Math.max(0, carbohydrate - o.carbohydrate),
        Math.max(0, protein - o.protein), Math.max(0, fat - o.fat));
  }

  public Nutrition times(double factor) {
    return new Nutrition(kcal * factor, carbohydrate * factor, protein * factor, fat * factor);
  }

  public Nutrition rounded() {
    return new Nutrition(r1(kcal), r1(carbohydrate), r1(protein), r1(fat));
  }

  private static double r1(double v) {
    return Math.round(v * 10) / 10.0;
  }
}
