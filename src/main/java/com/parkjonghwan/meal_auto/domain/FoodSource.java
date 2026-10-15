package com.parkjonghwan.meal_auto.domain;

// 음식 데이터가 어디서 왔는지 구분
public enum FoodSource {
  SEED,    // DataInitializer 기본 데이터 (공용)
  PUBLIC,  // 식약처 공공데이터에서 가져온 음식 (서버가 다시 조회해서 확인한 값만, 공용)
  USER,    // 회원이 직접 등록한 음식 (개인)
  AI       // AI 분석으로 새로 만들어진 음식 (개인, 잘못 추정했을 수 있음)
}
