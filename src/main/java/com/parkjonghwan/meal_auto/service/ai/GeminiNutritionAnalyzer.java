package com.parkjonghwan.meal_auto.service.ai;

import com.parkjonghwan.meal_auto.dto.AnalyzedFood;
import com.parkjonghwan.meal_auto.exception.AnalyzerUnavailableException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// Gemini API(무료 등급)로 식사 설명을 음식별 영양 정보로 분석
// - 결제 계정을 연결하지 않은 키만 사용 → 비용이 발생할 수 없음
// - 무료 한도 초과(429)는 AnalyzerUnavailableException → 사용자에게 "직접 입력" 안내(503)
@Component
public class GeminiNutritionAnalyzer implements NutritionAnalyzer {
  private static final Logger log = LoggerFactory.getLogger(GeminiNutritionAnalyzer.class);
  private static final JsonMapper JSON = JsonMapper.builder().build();

  // 응답 최대 길이. Gemini는 내부 사고 과정(thinking) 토큰도 이 한도에 포함되므로 넉넉하게 잡음
  private static final int MAX_OUTPUT_TOKENS = 4096;

  // 안전 정책으로 차단된 경우: 음식을 못 찾은 것으로 처리 (서비스에서 422)
  private static final Set<String> BLOCKED_FINISH_REASONS =
      Set.of("SAFETY", "RECITATION", "BLOCKLIST", "PROHIBITED_CONTENT", "SPII");

  // ▼ AI에게 주는 지시문: 결과 품질은 대부분 여기서 결정됨
  private static final String SYSTEM_PROMPT = """
      너는 한국 음식 영양 분석기다. 사용자가 먹은 음식 설명을 읽고 음식별로 나눠라.
      각 음식마다 아래 값을 채워라.
      - name: 수식어 없는 한국어 일반 명칭 (예: "김치찌개", "공기밥", "사과"). 브랜드명, 양, 맛 표현은 빼라.
      - grams: 실제로 먹은 양을 그램으로 추정. "한 그릇", "반 공기" 같은 표현은 한국 일반 1인분 기준으로 환산.
      - kcalPer100g, carbohydrate, protein, fat: 모두 100g 기준 값 (탄수화물·단백질·지방은 g).
      같은 음식을 두 번 쓰지 말고, 음식이 아닌 내용만 있으면 foods를 빈 배열로 돌려줘라.
      """;

  // ▼ AI 응답 모양 (모든 필드 필수)
  private static final Map<String, Object> RESPONSE_SCHEMA = Map.of(
      "type", "object",
      "properties", Map.of(
          "foods", Map.of(
              "type", "array",
              "items", Map.of(
                  "type", "object",
                  "properties", Map.of(
                      "name", Map.of("type", "string"),
                      "grams", Map.of("type", "number"),
                      "kcalPer100g", Map.of("type", "number"),
                      "carbohydrate", Map.of("type", "number"),
                      "protein", Map.of("type", "number"),
                      "fat", Map.of("type", "number")),
                  "required", List.of("name", "grams", "kcalPer100g", "carbohydrate", "protein", "fat")))),
      "required", List.of("foods"));

  // 연결 5초, 응답 30초 (AI는 공공데이터 API보다 응답이 느려서 길게 잡음)
  private final RestClient restClient = RestClient.builder()
      .requestFactory(timeoutFactory())
      .build();

  private final String baseUrl;
  private final String apiKey;
  private final String model;

  public GeminiNutritionAnalyzer(@Value("${gemini.base-url}") String baseUrl,
                                 @Value("${gemini.api-key:}") String apiKey,
                                 @Value("${gemini.model}") String model) {
    this.baseUrl = baseUrl;
    this.apiKey = apiKey;
    this.model = model;
  }

  private static SimpleClientHttpRequestFactory timeoutFactory() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(5));
    factory.setReadTimeout(Duration.ofSeconds(30));
    return factory;
  }

  @Override
  public List<AnalyzedFood> analyze(String text) {
    if (apiKey.isBlank()) {
      throw new AnalyzerUnavailableException("Gemini API 키(GEMINI_API_KEY)가 설정되지 않았어요");
    }

    Map<String, Object> requestBody = Map.of(
        "systemInstruction", Map.of("parts", List.of(Map.of("text", SYSTEM_PROMPT))),
        "contents", List.of(Map.of(
            "role", "user",
            "parts", List.of(Map.of("text", text)))),
        "generationConfig", Map.of(
            "responseMimeType", "application/json",
            "responseJsonSchema", RESPONSE_SCHEMA,
            "maxOutputTokens", MAX_OUTPUT_TOKENS));

    JsonNode root;
    try {
      root = restClient.post()
          .uri(baseUrl + "/" + model + ":generateContent")
          .header("x-goog-api-key", apiKey)
          .contentType(MediaType.APPLICATION_JSON)
          .body(requestBody)
          .retrieve()
          .body(JsonNode.class);
    } catch (RestClientResponseException e) {
      // 429(무료 한도 초과), 400(요청 형식 오류), 403(키 오류) 등
      // 원인 파악을 위해 응답 본문까지 로그에 남기고, 사용자에게는 일반 안내만 보냄
      if (e.getStatusCode().value() == 429) {
        log.warn("Gemini 무료 한도 초과(429)");
      } else {
        log.warn("Gemini API 오류 {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
      }
      throw new AnalyzerUnavailableException("Gemini API 호출에 실패했어요", e);
    } catch (RestClientException e) {
      // 시간 초과, 연결 실패 등
      log.warn("Gemini API 연결 실패: {}", e.getMessage());
      throw new AnalyzerUnavailableException("Gemini API에 연결하지 못했어요", e);
    }
    if (root == null) {
      throw new AnalyzerUnavailableException("Gemini 응답이 비어 있어요");
    }

    logUsage(root);
    return parseResponse(root);
  }

  // Gemini 응답 JSON → 음식 목록 (네트워크와 분리해서 테스트할 수 있게 패키지 공개로 둠)
  List<AnalyzedFood> parseResponse(JsonNode root) {
    // 입력 자체가 안전 정책으로 차단되면 candidates 없이 promptFeedback만 옴
    JsonNode blockReason = root.path("promptFeedback").path("blockReason");
    if (!blockReason.isMissingNode() && !blockReason.isNull()) {
      log.info("Gemini가 입력을 차단함: {}", blockReason.asString());
      return List.of();
    }

    JsonNode candidate = root.path("candidates").path(0);
    String finishReason = candidate.path("finishReason").asString();

    // 응답이 최대 길이에서 잘린 경우 → JSON이 불완전하므로 사용 불가
    if ("MAX_TOKENS".equals(finishReason)) {
      throw new AnalyzerUnavailableException("Gemini 응답이 중간에 잘렸어요");
    }
    if (BLOCKED_FINISH_REASONS.contains(finishReason)) {
      log.info("Gemini 응답이 차단됨: {}", finishReason);
      return List.of();
    }

    // parts 중 사고 과정(thought)이 아닌 실제 답변 텍스트만 이어 붙임
    StringBuilder content = new StringBuilder();
    for (JsonNode part : candidate.path("content").path("parts")) {
      if (!part.path("thought").asBoolean(false)) {
        content.append(part.path("text").asString());
      }
    }
    if (content.toString().isBlank()) {
      throw new AnalyzerUnavailableException("Gemini 응답에 내용이 없어요");
    }

    try {
      return toAnalyzedFoods(JSON.readTree(content.toString()));
    } catch (JacksonException e) {
      log.warn("Gemini 응답 해석 실패: {}", content);
      throw new AnalyzerUnavailableException("Gemini 응답을 해석하지 못했어요", e);
    }
  }

  private List<AnalyzedFood> toAnalyzedFoods(JsonNode result) {
    List<AnalyzedFood> foods = new ArrayList<>();
    for (JsonNode item : result.path("foods")) {
      foods.add(new AnalyzedFood(
          item.path("name").asString(),
          item.path("grams").asDouble(),
          item.path("kcalPer100g").asDouble(),
          item.path("carbohydrate").asDouble(),
          item.path("protein").asDouble(),
          item.path("fat").asDouble()));
    }
    return foods;
  }

  // 요청마다 토큰 사용량을 로그로 남김 (무료 한도 소진 속도 확인용)
  private void logUsage(JsonNode root) {
    JsonNode usage = root.path("usageMetadata");
    if (!usage.isMissingNode()) {
      log.info("Gemini 사용량 - model={}, 입력 토큰={}, 출력 토큰={}",
          model,
          usage.path("promptTokenCount").asInt(),
          usage.path("candidatesTokenCount").asInt());
    }
  }
}
