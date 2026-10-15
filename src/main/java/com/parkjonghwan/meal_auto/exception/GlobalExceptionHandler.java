package com.parkjonghwan.meal_auto.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

  // 공공데이터에 없는 음식을 가져오려 함 → 404
  @ExceptionHandler(PublicFoodNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlePublicFoodNotFound(PublicFoodNotFoundException e) {
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

  // AI 분석 사용 불가 (키 없음, 무료 한도 초과, AI 장애) → 503 Service Unavailable
  // 예외 메시지에는 "키가 잘못됨" 같은 내부 사정이 담길 수 있어 사용자에게는 고정 안내만 보냄
  // 화면에서는 이 응답을 받으면 "음식 직접 선택" 흐름으로 넘기면 됨
  @ExceptionHandler(AnalyzerUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleAnalyzerUnavailable(AnalyzerUnavailableException e) {
    // 사용자에게는 숨긴 진짜 원인을 서버 로그에는 남김 (원인 파악용)
    log.warn("AI 분석 사용 불가(503): {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(ErrorResponse.of(503, "지금은 AI 분석을 쓸 수 없어요. 음식을 직접 골라 기록해 주세요."));
  }

  // 입력은 정상인데 음식을 하나도 알아보지 못함 → 422 Unprocessable Entity
  @ExceptionHandler(NoFoodRecognizedException.class)
  public ResponseEntity<ErrorResponse> handleNoFoodRecognized(NoFoodRecognizedException e) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(ErrorResponse.of(422, "음식을 알아보지 못했어요. 음식 이름과 양을 조금 더 구체적으로 적어 주세요."));
  }
}
