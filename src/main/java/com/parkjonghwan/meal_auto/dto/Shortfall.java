package com.parkjonghwan.meal_auto.dto;

// 영양소 하나의 목표 대비 부족 정도 (ratio = 부족량 ÷ 목표, 음수면 목표 초과)
public record Shortfall(String nutrient, double target, double eaten, double lack, double ratio) {
}
