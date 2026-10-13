package com.parkjonghwan.meal_auto.service;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.dto.Recommendation;
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

    List<Food> candidates = foodRepository.findAll();
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
    double kcal = best.getKcalPer100g() * (grams / 100);
    if (kcal > remainingKcal) {
      grams = remainingKcal / best.getKcalPer100g() * 100;
    }
    grams = Math.round(grams);

    String reason = String.format("%s이 목표보다 %.1fg 부족해요", nutrient, lack);
    return new Recommendation(best, grams, reason);
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