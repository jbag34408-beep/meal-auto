package com.parkjonghwan.meal_auto.exception;

// 외부 API 호출 실패 (우리 서버 잘못이 아니라 외부 서버 문제)
public class ExternalApiException extends RuntimeException {
  public ExternalApiException(String message) {
    super(message);
  }
}