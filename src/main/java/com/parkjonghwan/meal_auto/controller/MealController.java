package com.parkjonghwan.meal_auto.controller;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.dto.MealEntryRequest;
import com.parkjonghwan.meal_auto.dto.Recommendation;
import com.parkjonghwan.meal_auto.exception.FoodNotFoundException;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import com.parkjonghwan.meal_auto.service.MealRecommendService;
import com.parkjonghwan.meal_auto.service.MealSummaryService;
import com.parkjonghwan.meal_auto.service.MemberService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MealController {
  private final FoodRepository foodRepository;
  private final MealEntryRepository mealEntryRepository;
  private final MealSummaryService summaryService;
  private final MealRecommendService recommendService;
  private final MemberService memberService;
  private final Clock clock;   // 한국 시간 (config/TimeConfig)

  public MealController(FoodRepository foodRepository,
                        MealEntryRepository mealEntryRepository,
                        MealSummaryService summaryService,
                        MealRecommendService recommendService,
                        MemberService memberService,
                        Clock clock) {
    this.foodRepository = foodRepository;
    this.mealEntryRepository = mealEntryRepository;
    this.summaryService = summaryService;
    this.recommendService = recommendService;
    this.memberService = memberService;
    this.clock = clock;
  }

  @PostMapping("/meals")
  public MealEntry addMeal(Principal principal, @Valid @RequestBody MealEntryRequest request) {
    Member member = memberService.get(principal.getName());
    // 다른 회원의 개인 음식은 "없는 음식"처럼 404
    Food food = foodRepository.findVisibleById(member, request.foodId())
        .orElseThrow(() -> new FoodNotFoundException(request.foodId()));
    return mealEntryRepository.save(new MealEntry(member, orToday(request.date()),
        request.mealType(), food, request.grams()));
  }

  // 남의 기록은 "없는 기록"처럼 404로 응답 (존재 여부도 알려주지 않음)
  @DeleteMapping("/meals/{id}")
  public ResponseEntity<Void> deleteMeal(Principal principal, @PathVariable Long id) {
    Member member = memberService.get(principal.getName());
    return mealEntryRepository.findById(id)
        .filter(entry -> entry.getMember() != null
            && entry.getMember().getId().equals(member.getId()))
        .map(entry -> {
          mealEntryRepository.delete(entry);
          return ResponseEntity.noContent().<Void>build();
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/meals")
  public List<MealEntry> meals(Principal principal,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    Member member = memberService.get(principal.getName());
    return mealEntryRepository.findByMemberAndDate(member, orToday(date));
  }

  @GetMapping("/summary")
  public DailySummary summary(Principal principal,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    Member member = memberService.get(principal.getName());
    return summaryService.summarizeDay(member, orToday(date));
  }

  @GetMapping("/recommendation")
  public Recommendation recommendation(Principal principal,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    Member member = memberService.get(principal.getName());
    return recommendService.recommend(summaryService.summarizeDay(member, orToday(date)), member);
  }

  private LocalDate orToday(LocalDate date) {
    return date != null ? date : LocalDate.now(clock);   // 날짜를 안 보내면 한국 날짜
  }
}