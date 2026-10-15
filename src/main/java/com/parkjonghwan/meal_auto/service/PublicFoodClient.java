package com.parkjonghwan.meal_auto.service;

import com.parkjonghwan.meal_auto.dto.FoodSearchResult;
import com.parkjonghwan.meal_auto.exception.ExternalApiException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;


// 식약처 식품영양성분 API를 호출하는 클래스 (외부 API 관련 코드를 이 파일 하나에 모음)
@Component
public class PublicFoodClient {

  // ▼ 응답 필드 이름: 실제 응답과 다르면 여기만 고치면 됨
  private static final String FIELD_NAME = "FOOD_NM_KR";
  private static final String FIELD_MAKER = "MAKER_NM";
  private static final String FIELD_BASIS = "NUTRI_AMOUNT_SERVING";
  private static final String FIELD_KCAL = "AMT_NUM1";
  private static final String FIELD_PROTEIN = "AMT_NUM3";
  private static final String FIELD_FAT = "AMT_NUM4";
  private static final String FIELD_CARBOHYDRATE = "AMT_NUM6";

  // 연결 5초, 응답 10초 안에 안 오면 포기 (외부 서버가 느리거나 막혀도 화면이 멈추지 않게)
  private final RestClient restClient = RestClient.builder()
      .requestFactory(timeoutFactory())
      .build();

  private final String baseUrl;
  private final String apiKey;

  public PublicFoodClient(@Value("${food-api.base-url}") String baseUrl,
                          @Value("${food-api.key:}") String apiKey) {
    this.baseUrl = baseUrl;
    this.apiKey = apiKey;
  }

  private static SimpleClientHttpRequestFactory timeoutFactory() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(5));
    factory.setReadTimeout(Duration.ofSeconds(10));
    return factory;
  }

  // 키가 없으면 검색·가져오기를 쓸 수 없음 (화면이 미리 알 수 있게)
  public boolean isConfigured() {
    return !apiKey.isBlank();
  }

  public List<FoodSearchResult> search(String keyword) {
    if (apiKey.isBlank()) {
      throw new ExternalApiException("공공데이터 API 키(FOOD_API_KEY)가 설정되지 않았어요");
    }

    // 인증키에 특수문자가 있을 수 있어서 반드시 인코딩
    // encode() → buildAndExpand() 순서여야 {key}, {name} 값이 안전하게 인코딩됨
    URI uri = UriComponentsBuilder.fromUriString(baseUrl)
        .queryParam("serviceKey", "{key}")
        .queryParam("FOOD_NM_KR", "{name}")
        .queryParam("pageNo", 1)
        .queryParam("numOfRows", 20)
        .queryParam("type", "json")
        .encode()
        .buildAndExpand(apiKey, keyword)
        .toUri();

    JsonNode root;
    try {
      root = restClient.get().uri(uri).retrieve().body(JsonNode.class);
    } catch (RestClientException e) {
      // 시간 초과, 연결 실패, 키 오류(JSON 대신 XML 응답) 모두 여기로 옴
      throw new ExternalApiException(
          "식품영양성분 API 호출에 실패했어요. 서버가 해외에 있거나 인증키가 아직 활성화되지 않았을 수 있어요.");
    }
    if (root == null) {
      throw new ExternalApiException("식품영양성분 API 응답이 비어 있어요");
    }

    String resultCode = root.path("header").path("resultCode").asString();
    if (!resultCode.isEmpty() && !resultCode.equals("00")) {
      throw new ExternalApiException("식품영양성분 API 오류: "
          + root.path("header").path("resultMsg").asString());
    }

    // 응답 모양이 items: [...] 또는 items: { item: [...] } 두 가지일 수 있어서 둘 다 처리
    JsonNode items = root.path("body").path("items");
    if (items.has("item")) {
      items = items.path("item");
    }

    List<FoodSearchResult> results = new ArrayList<>();
    if (items.isArray()) {
      for (JsonNode item : items) {
        results.add(toResult(item));
      }
    } else if (items.isObject()) {   // 결과가 1건이면 배열이 아닐 수 있음
      results.add(toResult(items));
    }
    return results;
  }

  private FoodSearchResult toResult(JsonNode item) {
    return new FoodSearchResult(
        item.path(FIELD_NAME).asString(),
        item.path(FIELD_MAKER).asString(),
        item.path(FIELD_BASIS).asString(),
        toDouble(item.path(FIELD_KCAL)),
        toDouble(item.path(FIELD_CARBOHYDRATE)),
        toDouble(item.path(FIELD_PROTEIN)),
        toDouble(item.path(FIELD_FAT)));
  }

  // "52", "1,234", "-", "" 같은 값을 숫자로 바꾸기 (못 바꾸면 0)
  private double toDouble(JsonNode node) {
    String text = node.asString().trim().replace(",", "");
    try {
      return Math.round(Double.parseDouble(text) * 10) / 10.0;
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}