package com.parkjonghwan.meal_auto.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 모든 컨트롤러에서 던져진 예외를 여기서 한 번에 받아 응답으로 바꿈
@RestControllerAdvice
public class GlobalExceptionHandler {

  // @Valid 검증 실패 → 400
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
    Map<String, String> errors = new LinkedHashMap<>();
    for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
      errors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(400, "입력값이 올바르지 않아요", errors));
  }

  // JSON 문법 오류, enum 오타, 날짜 형식 오류 → 400
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException e) {
    return ResponseEntity.badRequest()
        .body(ErrorResponse.of(400,
            "요청 형식이 올바르지 않아요 (mealType은 BREAKFAST, LUNCH, DINNER, SNACK 중 하나)"));
  }

  // 없는 음식 → 404
  @ExceptionHandler(FoodNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleFoodNotFound(FoodNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of(404, e.getMessage()));
  }
    // 같은 이름 음식 → 409 Conflict (이미 있는 것과 충돌)
  @ExceptionHandler(DuplicateFoodException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateFood(DuplicateFoodException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of(409, e.getMessage()));
  }

  // 영양소 조합이 말이 안 됨 → 400
  @ExceptionHandler(InvalidNutritionException.class)
  public ResponseEntity<ErrorResponse> handleInvalidNutrition(InvalidNutritionException e) {
    return ResponseEntity.badRequest()
        .body(ErrorResponse.of(400, e.getMessage()));
  }
    // 외부 API 실패 → 502 Bad Gateway (중간 서버가 외부 서버로부터 제대로 된 응답을 못 받음)
  @ExceptionHandler(ExternalApiException.class)
  public ResponseEntity<ErrorResponse> handleExternalApi(ExternalApiException e) {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
        .body(ErrorResponse.of(502, e.getMessage()));
  }
    // 아이디 중복 → 409
  @ExceptionHandler(DuplicateMemberException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateMember(DuplicateMemberException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of(409, e.getMessage()));
  }
}
