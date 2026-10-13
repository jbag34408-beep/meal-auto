package com.parkjonghwan.meal_auto.controller;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.dto.MealEntryRequest;
import com.parkjonghwan.meal_auto.dto.Recommendation;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import com.parkjonghwan.meal_auto.service.MealRecommendService;
import com.parkjonghwan.meal_auto.service.MealSummaryService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import com.parkjonghwan.meal_auto.exception.FoodNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@RestController
public class MealController {
  private final FoodRepository foodRepository;
  private final MealEntryRepository mealEntryRepository;
  private final MealSummaryService summaryService;
  private final MealRecommendService recommendService;

  public MealController(FoodRepository foodRepository,
                        MealEntryRepository mealEntryRepository,
                        MealSummaryService summaryService,
                        MealRecommendService recommendService) {
    this.foodRepository = foodRepository;
    this.mealEntryRepository = mealEntryRepository;
    this.summaryService = summaryService;
    this.recommendService = recommendService;
  }

  
  

  // POST /meals → 식사 기록 추가
    @PostMapping("/meals")
  public MealEntry addMeal(@Valid @RequestBody MealEntryRequest request) {   // @Valid 추가
    Food food = foodRepository.findById(request.foodId())
        .orElseThrow(() -> new FoodNotFoundException(request.foodId()));     // 전용 예외로 변경
    return mealEntryRepository.save(
        new MealEntry(orToday(request.date()), request.mealType(), food, request.grams()));
  }
  // GET /meals?date=2026-10-12 → 그날 기록 (생략 시 오늘)
  @GetMapping("/meals")
  public List<MealEntry> meals(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return mealEntryRepository.findByDate(orToday(date));
  }
    // DELETE /meals/3 → 3번 기록 삭제
  @DeleteMapping("/meals/{id}")
  public ResponseEntity<Void> deleteMeal(@PathVariable Long id) {
    if (!mealEntryRepository.existsById(id)) {
      return ResponseEntity.notFound().build();   // 404
    }
    mealEntryRepository.deleteById(id);
    return ResponseEntity.noContent().build();    // 204: 성공, 돌려줄 내용 없음
  }

  // GET /summary → 하루 요약
  @GetMapping("/summary")
  public DailySummary summary(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return summaryService.summarizeDay(orToday(date));
  }

  // GET /recommendation → 다음 끼니 추천
  @GetMapping("/recommendation")
  public Recommendation recommendation(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return recommendService.recommend(summaryService.summarizeDay(orToday(date)));
  }

  private LocalDate orToday(LocalDate date) {
    return date != null ? date : LocalDate.now();
  }
}
