package com.parkjonghwan.meal_auto.dto;

// 추천 음식을 그 양만큼 먹었을 때 영양소 하나에 생기는 변화 (g)
// added = filled + over
public record NutrientEffect(
    String nutrient,
    double added,    // 이 음식으로 더해지는 양
    double filled,   // 그중 부족분을 채우는 양
    double over      // 그중 목표를 넘는 양
) {
}
