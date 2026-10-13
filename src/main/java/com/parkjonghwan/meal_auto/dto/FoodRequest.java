package com.parkjonghwan.meal_auto.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record FoodRequest(
    @NotBlank(message = "음식 이름은 필수예요")
    @Size(max = 50, message = "음식 이름은 50자 이하여야 해요")
    String name,

    @PositiveOrZero(message = "칼로리는 0 이상이어야 해요")
    @Max(value = 900, message = "100g당 칼로리는 900kcal 이하여야 해요")   // 순수 지방도 약 900kcal
    double kcalPer100g,

    @PositiveOrZero(message = "탄수화물은 0 이상이어야 해요")
    @Max(value = 100, message = "100g당 탄수화물은 100g 이하여야 해요")
    double carbohydrate,

    @PositiveOrZero(message = "단백질은 0 이상이어야 해요")
    @Max(value = 100, message = "100g당 단백질은 100g 이하여야 해요")
    double protein,

    @PositiveOrZero(message = "지방은 0 이상이어야 해요")
    @Max(value = 100, message = "100g당 지방은 100g 이하여야 해요")
    double fat
) {
}