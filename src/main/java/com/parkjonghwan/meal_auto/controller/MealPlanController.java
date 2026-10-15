package com.parkjonghwan.meal_auto.controller;

import com.parkjonghwan.meal_auto.dto.MealPlanResponse;
import com.parkjonghwan.meal_auto.service.MealPlanService;
import com.parkjonghwan.meal_auto.service.MemberService;
import java.security.Principal;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// GET /meal-plan?date=2026-10-15 → 아직 안 먹은 아침·점심·저녁의 한식 상차림 추천
@RestController
public class MealPlanController {
  private final MealPlanService mealPlanService;
  private final MemberService memberService;
  private final Clock clock;

  public MealPlanController(MealPlanService mealPlanService, MemberService memberService, Clock clock) {
    this.mealPlanService = mealPlanService;
    this.memberService = memberService;
    this.clock = clock;
  }

  @GetMapping("/meal-plan")
  public MealPlanResponse plan(Principal principal,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    LocalDate day = date != null ? date : LocalDate.now(clock);
    return mealPlanService.plan(memberService.get(principal.getName()), day);
  }
}
