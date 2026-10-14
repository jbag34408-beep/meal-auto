package com.parkjonghwan.meal_auto.service.ai;

import com.parkjonghwan.meal_auto.dto.AnalyzedFood;
import java.util.List;

// 식사 설명 텍스트를 음식별 영양 정보로 바꾸는 분석기
// 실제 서비스에서는 Gemini 구현체, 테스트에서는 가짜 구현체를 끼워 씀
// → AI 업체를 바꿔도 이 인터페이스를 쓰는 서비스·컨트롤러·테스트는 그대로
public interface NutritionAnalyzer {

  // 분석할 수 없는 상황(키 없음, 무료 한도 초과, AI 장애 등)이면 AnalyzerUnavailableException을 던짐
  // 음식을 하나도 못 찾았으면 빈 목록을 돌려줌
  List<AnalyzedFood> analyze(String text);
}
