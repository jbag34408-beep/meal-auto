package com.parkjonghwan.meal_auto.dto;

import com.parkjonghwan.meal_auto.domain.MealType;
import java.time.LocalDate;

// 식사 기록 추가 요청 데이터 (record: 필드·생성자·getter 자동 생성)
public record MealEntryRequest(LocalDate date, MealType mealType, Long foodId, double grams) {
}
