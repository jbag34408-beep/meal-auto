package com.parkjonghwan.meal_auto.dto;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.FoodSource;
import com.parkjonghwan.meal_auto.domain.Member;

// AI가 분석한 음식 하나 (영양 정보는 모두 100g 기준, grams는 실제 먹은 양 추정)
public record AnalyzedFood(
    String name,
    double grams,
    double kcalPer100g,
    double carbohydrate,
    double protein,
    double fat
) {

  // AI 응답은 틀릴 수 있으므로 저장 전에 말이 되는 값인지 검사
  public boolean isValid() {
    return name != null && !name.isBlank()
        && grams > 0 && grams <= 5000            // MealEntryRequest와 같은 상한
        && kcalPer100g >= 0 && kcalPer100g <= 900 // 100g당 900kcal 초과는 사실상 불가능 (순수 지방이 약 900)
        && carbohydrate >= 0 && protein >= 0 && fat >= 0;
  }

  // 이름 앞뒤 공백 제거 (기존 음식과 이름을 맞출 때 사용)
  public String normalizedName() {
    return name.strip();
  }

  // 새 음식으로 저장할 때 AI 출처의 개인 음식으로 생성 (잘못 추정했을 수 있어 다른 회원과 공유하지 않음)
  public Food toFood(Member owner) {
    return new Food(normalizedName(), kcalPer100g, carbohydrate, protein, fat, FoodSource.AI, owner);
  }
}
