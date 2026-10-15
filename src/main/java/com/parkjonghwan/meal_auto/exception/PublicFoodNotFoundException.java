package com.parkjonghwan.meal_auto.exception;

// 가져오려는 음식이 공공데이터 검색 결과에 없음 (화면이 보낸 값을 그대로 믿지 않기 위해 다시 조회함)
public class PublicFoodNotFoundException extends RuntimeException {
  public PublicFoodNotFoundException(String name) {
    super("공공데이터에서 찾을 수 없는 음식이에요: " + name);
  }
}
