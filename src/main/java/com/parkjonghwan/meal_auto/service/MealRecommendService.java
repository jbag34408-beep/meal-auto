package com.parkjonghwan.meal_auto.service;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.FoodSource;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.dto.Recommendation;
import com.parkjonghwan.meal_auto.dto.Shortfall;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MealRecommendService {
  private final FoodRepository foodRepository;

  public MealRecommendService(FoodRepository foodRepository) {
    this.foodRepository = foodRepository;
  }

  // 고정 상수 대신 회원 각자의 목표로 계산
  public Recommendation recommend(DailySummary summary, Member member) {
    double remainingKcal = member.getTargetKcal() - summary.getTotalKcal();
    if (remainingKcal <= 0) {
      return null;
    }

    double carbohydrateLack = member.getTargetCarbohydrate() - summary.getTotalCarbohydrate();
    double proteinLack = member.getTargetProtein() - summary.getTotalProtein();
    double fatLack = member.getTargetFat() - summary.getTotalFat();

    double carbohydrateRatio = carbohydrateLack / member.getTargetCarbohydrate();
    double proteinRatio = proteinLack / member.getTargetProtein();
    double fatRatio = fatLack / member.getTargetFat();

    List<Shortfall> shortfalls = List.of(
        shortfall("탄수화물", member.getTargetCarbohydrate(), summary.getTotalCarbohydrate(),
            carbohydrateLack, carbohydrateRatio),
        shortfall("단백질", member.getTargetProtein(), summary.getTotalProtein(),
            proteinLack, proteinRatio),
        shortfall("지방", member.getTargetFat(), summary.getTotalFat(), fatLack, fatRatio));

    String nutrient;
    double lack;
    if (carbohydrateRatio >= proteinRatio && carbohydrateRatio >= fatRatio) {
      nutrient = "탄수화물";
      lack = carbohydrateLack;
    } else if (proteinRatio >= fatRatio) {
      nutrient = "단백질";
      lack = proteinLack;
    } else {
      nutrient = "지방";
      lack = fatLack;
    }

    if (lack <= 0) {
      return null;
    }

    // 후보: 공용 음식 + 내 음식만 (다른 회원이 등록하거나 AI로 만든 음식은 제외)
    List<Food> candidates = foodRepository.findVisibleTo(member).stream()
        .filter(MealRecommendService::isTrustedFor)
        .toList();
    Food best = null;
    double bestAmount = 0;
    for (Food food : candidates) {
      double amount = amountPer100g(food, nutrient);
      if (amount > bestAmount) {
        best = food;
        bestAmount = amount;
      }
    }
    if (best == null) {
      return null;
    }

    double grams = lack / bestAmount * 100;
    double neededGrams = Math.round(grams);
    double kcal = best.getKcalPer100g() * (grams / 100);
    boolean capped = kcal > remainingKcal;
    if (capped) {
      grams = remainingKcal / best.getKcalPer100g() * 100;
    }
    grams = Math.round(grams);
    if (grams < 1) {   // 1g도 안 되는 양은 추천하지 않음 (기록 최소량도 1g)
      return null;
    }

    String reason = String.format("%s이 목표보다 %.1fg 부족해요", nutrient, lack);
    return new Recommendation(best, grams, reason, nutrient, neededGrams,
        round1(best.getKcalPer100g() * grams / 100), round1(remainingKcal), capped, shortfalls);
  }

  // 주인 기능 이전에 만들어져 주인을 알 수 없는 AI 음식이 공용으로 남아 있을 수 있음
  // → 공용인데 AI 출처면 값을 확인할 수 없으므로 추천하지 않음 (내 AI 음식은 내 추천에 써도 됨)
  private static boolean isTrustedFor(Food food) {
    return !(food.isShared() && food.getSource() == FoodSource.AI);
  }

  private Shortfall shortfall(String nutrient, double target, double eaten,
                              double lack, double ratio) {
    return new Shortfall(nutrient, target, round1(eaten), round1(lack),
        Math.round(ratio * 1000) / 1000.0);
  }

  private double round1(double value) {
    return Math.round(value * 10) / 10.0;
  }

  private double amountPer100g(Food food, String nutrient) {
    if (nutrient.equals("탄수화물")) {
      return food.getCarbohydrate();
    } else if (nutrient.equals("단백질")) {
      return food.getProtein();
    } else {
      return food.getFat();
    }
  }
}