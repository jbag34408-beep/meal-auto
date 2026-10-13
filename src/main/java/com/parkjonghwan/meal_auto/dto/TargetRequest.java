package com.parkjonghwan.meal_auto.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

// 목표값은 0이면 추천 계산에서 0으로 나누게 되므로 모두 양수만 허용
public record TargetRequest(
    @Min(value = 500, message = "목표 칼로리는 500kcal 이상이어야 해요")
    @Max(value = 10000, message = "목표 칼로리는 10000kcal 이하여야 해요")
    double kcal,

    @Positive(message = "탄수화물 목표는 0보다 커야 해요")
    @Max(value = 1000, message = "탄수화물 목표는 1000g 이하여야 해요")
    double carbohydrate,

    @Positive(message = "단백질 목표는 0보다 커야 해요")
    @Max(value = 500, message = "단백질 목표는 500g 이하여야 해요")
    double protein,

    @Positive(message = "지방 목표는 0보다 커야 해요")
    @Max(value = 500, message = "지방 목표는 500g 이하여야 해요")
    double fat
) {
}