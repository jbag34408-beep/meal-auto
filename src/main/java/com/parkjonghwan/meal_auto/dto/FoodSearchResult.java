package com.parkjonghwan.meal_auto.dto;

// 공공 API 검색 결과 한 건 (우리 DB에 저장되기 전 상태)
public record FoodSearchResult(
    String name,
    String maker,          // 제조사 (일반 식품은 비어 있음)
    String basis,          // 영양 성분 기준량 (예: 100g)
    double kcal,
    double carbohydrate,
    double protein,
    double fat
) {
}
