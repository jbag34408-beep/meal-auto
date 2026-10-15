package com.parkjonghwan.meal_auto.domain;

// 한 끼 한식 상차림에서 음식이 들어가는 자리 (식단 추천에 씀)
public enum FoodCategory {
  RICE("밥"),
  SOUP("국·찌개"),
  MAIN("주반찬"),
  SIDE("나물·볶음"),
  KIMCHI("김치");

  private final String label;

  FoodCategory(String label) {
    this.label = label;
  }

  public String label() {
    return label;
  }
}
