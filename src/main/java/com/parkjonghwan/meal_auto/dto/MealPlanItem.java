package com.parkjonghwan.meal_auto.dto;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.FoodCategory;

// 상차림의 음식 하나 (예: 잡곡밥 158g)
public record MealPlanItem(
    Food food,              // 분류는 food.category에 있음
    String categoryLabel,   // 밥, 국·찌개, 주반찬, 나물·볶음, 김치
    double grams,
    Nutrition nutrition,
    boolean recent          // 최근 3일 안에 먹은 음식
) {
  // 서버 안에서 쓰는 지름길 (JSON에는 안 나감)
  public FoodCategory category() {
    return food.getCategory();
  }
}
