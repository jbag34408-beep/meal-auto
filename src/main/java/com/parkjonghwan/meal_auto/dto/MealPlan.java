package com.parkjonghwan.meal_auto.dto;

import com.parkjonghwan.meal_auto.domain.MealType;
import java.util.List;

// 한 끼 한식 상차림 추천 (밥 + 국·찌개 + 주반찬 + 나물 + 김치)
public record MealPlan(
    MealType mealType,
    Nutrition target,        // 이 끼니에 먹으면 좋은 양 (남은 양을 남은 끼니에 나눈 값)
    Nutrition total,         // 상차림 합계
    List<MealPlanItem> items
) {
}
