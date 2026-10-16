package com.parkjonghwan.meal_auto.controller;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.PublicFood;
import com.parkjonghwan.meal_auto.dto.FoodRequest;
import com.parkjonghwan.meal_auto.dto.PublicFoodImportRequest;
import com.parkjonghwan.meal_auto.service.FoodService;
import com.parkjonghwan.meal_auto.service.MemberService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/foods")
public class FoodController {
  private final FoodService foodService;
  private final MemberService memberService;

  public FoodController(FoodService foodService, MemberService memberService) {
    this.foodService = foodService;
    this.memberService = memberService;
  }

  // GET /foods  또는  GET /foods?keyword=닭 → 공용 음식 + 내 음식
  @GetMapping
  public List<Food> search(Principal principal, @RequestParam(required = false) String keyword) {
    return foodService.search(memberService.get(principal.getName()), keyword);
  }

  // POST /foods → 201 Created (내 개인 음식으로 등록)
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Food create(Principal principal, @Valid @RequestBody FoodRequest request) {
    return foodService.create(memberService.get(principal.getName()), request);
  }

  // GET /foods/public/status → {"available": true/false} (식약처 DB가 들어 있는지)
  @GetMapping("/public/status")
  public Map<String, Boolean> publicStatus() {
    return Map.of("available", foodService.hasPublicFoods());
  }

  // GET /foods/public?keyword=사과 → 식약처 DB 검색 (저장은 안 함)
  @GetMapping("/public")
  public List<PublicFood> searchPublic(@RequestParam String keyword) {
    return foodService.searchPublic(keyword);
  }

  // POST /foods/public {"name":"사과","maker":""} → 공용 음식으로 가져오기
  // 영양 값은 서버가 내장된 식약처 DB에서 다시 찾아서 채움
  @PostMapping("/public")
  public Food importPublic(@Valid @RequestBody PublicFoodImportRequest request) {
    return foodService.importPublic(request);
  }
}
