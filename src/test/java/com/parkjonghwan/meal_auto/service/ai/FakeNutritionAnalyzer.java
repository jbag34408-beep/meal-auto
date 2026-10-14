package com.parkjonghwan.meal_auto.service.ai;

import com.parkjonghwan.meal_auto.dto.AnalyzedFood;
import com.parkjonghwan.meal_auto.exception.AnalyzerUnavailableException;
import java.util.List;

// 테스트용 가짜 분석기: 실제 Gemini를 부르지 않고 미리 정해 둔 결과를 돌려줌
// 무료 한도를 안 쓰고, 매번 같은 결과가 나와서 테스트가 흔들리지 않음
public class FakeNutritionAnalyzer implements NutritionAnalyzer {

  private List<AnalyzedFood> nextResult = List.of();
  private boolean failNext = false;

  // 다음 분석 때 이 음식들을 돌려주도록 설정
  public void willReturn(AnalyzedFood... foods) {
    this.nextResult = List.of(foods);
    this.failNext = false;
  }

  // 다음 분석 때 AI 장애가 난 것처럼 예외를 던지도록 설정
  public void willFail() {
    this.failNext = true;
  }

  // 테스트마다 처음 상태로 되돌림
  public void reset() {
    this.nextResult = List.of();
    this.failNext = false;
  }

  @Override
  public List<AnalyzedFood> analyze(String text) {
    if (failNext) {
      throw new AnalyzerUnavailableException("테스트용 AI 장애");
    }
    return nextResult;
  }
}
