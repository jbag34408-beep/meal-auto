package com.parkjonghwan.meal_auto.dto;

import com.parkjonghwan.meal_auto.domain.Food;
import java.util.List;

// 추천 후보 하나와 점수 근거
// score = fillScore - overPenalty - kcalPenalty - recentPenalty (클수록 좋음)
public record RecommendationOption(
    Food food,
    double grams,
    double kcal,
    boolean capped,                 // 한 끼 양(500g·한 끼 칼로리·남은 칼로리)에 맞춰 줄였는지
    boolean recent,                 // 최근 3일 안에 먹은 음식인지
    List<NutrientEffect> effects,   // 탄수화물·단백질·지방 순서
    double score,
    double fillScore,               // 부족분을 채운 정도 (가장 부족한 영양소 비율 + 다른 영양소 비율 × 0.25)
    double overPenalty,             // 목표를 넘긴 정도 (목표 대비 비율의 합)
    double kcalPenalty,             // 남은 칼로리를 쓰는 부담
    double recentPenalty,           // 최근에 먹은 음식이면 깎는 점수
    String summary                  // 화면에 보여 줄 한 줄 설명
) {
}
