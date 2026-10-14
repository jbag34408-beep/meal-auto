package com.parkjonghwan.meal_auto.service.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.parkjonghwan.meal_auto.dto.AnalyzedFood;
import com.parkjonghwan.meal_auto.exception.AnalyzerUnavailableException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// Gemini 응답 JSON을 해석하는 부분만 따로 검사 (네트워크·API 키 없이 실행)
// 실제 Gemini 응답 모양: candidates[0].content.parts[].text 안에 우리가 요청한 JSON이 문자열로 들어 있음
class GeminiNutritionAnalyzerTest {

  private static final JsonMapper JSON = JsonMapper.builder().build();

  private final GeminiNutritionAnalyzer analyzer =
      new GeminiNutritionAnalyzer("https://example.invalid", "", "test-model");

  private JsonNode response(String json) {
    return JSON.readTree(json);
  }

  // parts[].text에 들어갈 답변 JSON 문자열 (따옴표를 이스케이프해서 응답 안에 넣음)
  private static final String ANSWER =
      "{\\\"foods\\\":[{\\\"name\\\":\\\"김치찌개\\\",\\\"grams\\\":300,\\\"kcalPer100g\\\":50,"
          + "\\\"carbohydrate\\\":3,\\\"protein\\\":4,\\\"fat\\\":2.5}]}";

  @Test
  @DisplayName("정상 응답에서 음식 목록을 꺼낸다")
  void parsesFoods() {
    List<AnalyzedFood> foods = analyzer.parseResponse(response("""
        {"candidates":[{"finishReason":"STOP",
          "content":{"parts":[{"text":"%s"}]}}]}
        """.formatted(ANSWER)));

    assertThat(foods).hasSize(1);
    AnalyzedFood food = foods.get(0);
    assertThat(food.name()).isEqualTo("김치찌개");
    assertThat(food.grams()).isEqualTo(300);
    assertThat(food.kcalPer100g()).isEqualTo(50);
    assertThat(food.fat()).isEqualTo(2.5);
  }

  @Test
  @DisplayName("사고 과정(thought) 파트는 무시하고 실제 답변만 해석한다")
  void ignoresThoughtParts() {
    List<AnalyzedFood> foods = analyzer.parseResponse(response("""
        {"candidates":[{"finishReason":"STOP",
          "content":{"parts":[
            {"text":"먼저 음식을 나눠 보자...","thought":true},
            {"text":"%s"}]}}]}
        """.formatted(ANSWER)));

    assertThat(foods).extracting(AnalyzedFood::name).containsExactly("김치찌개");
  }

  @Test
  @DisplayName("응답이 길이 제한에서 잘리면 사용 불가로 처리한다")
  void maxTokensIsUnavailable() {
    assertThatThrownBy(() -> analyzer.parseResponse(response("""
        {"candidates":[{"finishReason":"MAX_TOKENS",
          "content":{"parts":[{"text":"{\\"foods\\":[{\\"name\\":"}]}}]}
        """)))
        .isInstanceOf(AnalyzerUnavailableException.class);
  }

  @Test
  @DisplayName("안전 정책으로 응답이 차단되면 빈 목록을 돌려준다")
  void safetyBlockReturnsEmpty() {
    List<AnalyzedFood> foods = analyzer.parseResponse(response("""
        {"candidates":[{"finishReason":"SAFETY","content":{"parts":[]}}]}
        """));

    assertThat(foods).isEmpty();
  }

  @Test
  @DisplayName("입력 자체가 차단되면(promptFeedback) 빈 목록을 돌려준다")
  void promptBlockReturnsEmpty() {
    List<AnalyzedFood> foods = analyzer.parseResponse(response("""
        {"promptFeedback":{"blockReason":"SAFETY"}}
        """));

    assertThat(foods).isEmpty();
  }

  @Test
  @DisplayName("답변이 JSON이 아니면 사용 불가로 처리한다")
  void brokenJsonIsUnavailable() {
    assertThatThrownBy(() -> analyzer.parseResponse(response("""
        {"candidates":[{"finishReason":"STOP",
          "content":{"parts":[{"text":"죄송해요, 잘 모르겠어요"}]}}]}
        """)))
        .isInstanceOf(AnalyzerUnavailableException.class);
  }

  @Test
  @DisplayName("Google 쪽 일시 오류(500/503/504)만 다시 시도한다")
  void retriesOnlyTransientErrors() {
    assertThat(GeminiNutritionAnalyzer.isRetryable(503)).isTrue();
    assertThat(GeminiNutritionAnalyzer.isRetryable(500)).isTrue();
    assertThat(GeminiNutritionAnalyzer.isRetryable(504)).isTrue();
    assertThat(GeminiNutritionAnalyzer.isRetryable(429)).isFalse();
    assertThat(GeminiNutritionAnalyzer.isRetryable(404)).isFalse();
    assertThat(GeminiNutritionAnalyzer.isRetryable(400)).isFalse();
  }

  @Test
  @DisplayName("API 키가 없으면 Gemini를 부르지 않고 바로 사용 불가로 처리한다")
  void missingKeyIsUnavailable() {
    assertThatThrownBy(() -> analyzer.analyze("김치찌개 한 그릇"))
        .isInstanceOf(AnalyzerUnavailableException.class);
  }
}
