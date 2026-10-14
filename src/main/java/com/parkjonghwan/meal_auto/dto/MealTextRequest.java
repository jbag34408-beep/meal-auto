package com.parkjonghwan.meal_auto.dto;

import com.parkjonghwan.meal_auto.domain.MealType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

// 텍스트로 식사 기록 요청 ("김치찌개 한 그릇이랑 밥 반 공기")
public record MealTextRequest(
    LocalDate date,   // 생략 가능 (생략 시 오늘)

    @NotNull(message = "끼니(mealType)는 필수예요")
    MealType mealType,

    @NotBlank(message = "먹은 음식(text)을 입력해 주세요")
    @Size(max = 300, message = "음식 설명은 300자 이하로 입력해 주세요")   // 긴 글일수록 AI 무료 한도를 빨리 씀
    String text
) {
}
