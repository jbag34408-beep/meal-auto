package com.parkjonghwan.meal_auto.dto;

import java.time.LocalDate;
import java.util.List;

// 하루 식단 추천: 아직 안 먹은 끼니마다 상차림 하나
public record MealPlanResponse(
    LocalDate date,
    Nutrition remaining,     // 오늘 목표까지 남은 양
    List<MealPlan> meals,    // 비어 있으면 message 참고
    String message
) {
}
