package com.parkjonghwan.meal_auto.controller;

import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.MealTextRequest;
import com.parkjonghwan.meal_auto.service.MealAnalysisService;
import com.parkjonghwan.meal_auto.service.MemberService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// 텍스트로 식사 기록: {"mealType":"LUNCH","text":"김치찌개 한 그릇이랑 밥 반 공기"}
// 기존 MealController(음식 id로 직접 기록)는 그대로 두고 따로 분리
@RestController
public class MealAnalysisController {
  private final MealAnalysisService mealAnalysisService;
  private final MemberService memberService;

  public MealAnalysisController(MealAnalysisService mealAnalysisService,
                                MemberService memberService) {
    this.mealAnalysisService = mealAnalysisService;
    this.memberService = memberService;
  }

  // 성공 → 200 + 저장된 기록 목록
  // AI 사용 불가 → 503 / 음식을 못 찾음 → 422 (GlobalExceptionHandler가 처리)
  @PostMapping("/meals/analyze")
  public List<MealEntry> analyze(Principal principal,
                                 @Valid @RequestBody MealTextRequest request) {
    Member member = memberService.get(principal.getName());
    return mealAnalysisService.recordFromText(member, request);
  }
}
