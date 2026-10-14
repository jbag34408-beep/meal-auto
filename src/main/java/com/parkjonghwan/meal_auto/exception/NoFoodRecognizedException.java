package com.parkjonghwan.meal_auto.exception;

// AI가 분석은 했지만 저장할 수 있는 음식을 하나도 찾지 못했을 때
// 컨트롤러에서 422로 바꿔서 "음식을 알아보지 못했어요" 안내로 연결
public class NoFoodRecognizedException extends RuntimeException {

  public NoFoodRecognizedException(String text) {
    super("입력한 내용에서 음식을 찾지 못했어요: " + text);
  }
}
