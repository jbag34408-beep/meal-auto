package com.parkjonghwan.meal_auto.domain;

// 음식 데이터가 어디서 왔는지 구분
public enum FoodSource {
  SEED,  // AI가 아닌 경로로 들어온 음식 (DataInitializer 기본 데이터, 직접 등록, 공공데이터 가져오기)
  AI     // AI 분석으로 새로 만들어진 음식 (나중에 검수하거나 골라 지울 수 있게 표시)
}
