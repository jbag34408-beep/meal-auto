package com.parkjonghwan.meal_auto.dto;

import com.parkjonghwan.meal_auto.domain.MealType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

// 추천 상차림을 한 번에 기록 (음식 여러 개를 같은 끼니로)
public record MealBulkRequest(
    LocalDate date,   // 생략 가능 (생략 시 오늘)

    @NotNull(message = "끼니(mealType)는 필수예요")
    MealType mealType,

    @NotEmpty(message = "기록할 음식이 없어요")
    @Size(max = 10, message = "한 번에 10가지까지 기록할 수 있어요")
    List<@Valid Item> items
) {
  public record Item(
      @NotNull(message = "음식 id(foodId)는 필수예요")
      @Positive(message = "음식 id는 1 이상이어야 해요")
      Long foodId,

      @Positive(message = "섭취량은 0보다 커야 해요")
      @Max(value = 5000, message = "섭취량은 5000g 이하여야 해요")
      double grams
  ) {
  }
}
