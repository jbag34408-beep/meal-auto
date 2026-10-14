package com.parkjonghwan.meal_auto.exception;

// AI 분석을 쓸 수 없을 때 (API 키 없음, 무료 한도 초과, AI 서버 장애, 응답 해석 실패 등)
// 컨트롤러에서 503으로 바꿔서 "음식을 직접 골라 기록해 주세요" 안내로 연결
public class AnalyzerUnavailableException extends RuntimeException {

  public AnalyzerUnavailableException(String message) {
    super(message);
  }

  public AnalyzerUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
