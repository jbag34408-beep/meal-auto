package com.parkjonghwan.meal_auto.exception;

import java.util.Map;

// 모든 에러 응답의 공통 모양
// errors: 어느 필드가 왜 틀렸는지 (검증 에러일 때만 채움)
public record ErrorResponse(int status, String message, Map<String, String> errors) {

  public static ErrorResponse of(int status, String message) {
    return new ErrorResponse(status, message, Map.of());
  }
}