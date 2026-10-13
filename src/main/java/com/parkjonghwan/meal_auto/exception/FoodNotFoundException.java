package com.parkjonghwan.meal_auto.exception;

// "음식이 없음"을 뜻하는 전용 예외 (어떤 에러인지 이름으로 구분하기 위해)
public class FoodNotFoundException extends RuntimeException {
  public FoodNotFoundException(Long foodId) {
    super("없는 음식이에요: id=" + foodId);
  }
}