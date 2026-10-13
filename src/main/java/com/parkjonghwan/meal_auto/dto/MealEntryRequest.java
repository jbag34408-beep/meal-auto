package com.parkjonghwan.meal_auto.dto;

import com.parkjonghwan.meal_auto.domain.MealType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

// 어노테이션으로 "들어와도 되는 값"의 규칙을 선언
public record MealEntryRequest(
    LocalDate date,   // 생략 가능 (생략 시 오늘)

    @NotNull(message = "끼니(mealType)는 필수예요")
    MealType mealType,

    @NotNull(message = "음식 id(foodId)는 필수예요")
    @Positive(message = "음식 id는 1 이상이어야 해요")
    Long foodId,

    @Positive(message = "섭취량은 0보다 커야 해요")
    @Max(value = 5000, message = "섭취량은 5000g 이하여야 해요")
    double grams
) {
}