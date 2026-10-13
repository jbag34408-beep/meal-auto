package com.parkjonghwan.meal_auto.service;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.dto.Recommendation;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MealRecommendService {
  private static final double TARGET_KCAL = 2000;
  private static final double TARGET_CARBOHYDRATE = 300;
  private static final double TARGET_PROTEIN = 60;
  private static final double TARGET_FAT = 50;

  private final FoodRepository foodRepository;

  public MealRecommendService(FoodRepository foodRepository) {
    this.foodRepository = foodRepository;
  }

  public Recommendation recommend(DailySummary summary) {
    double remainingKcal = TARGET_KCAL - summary.getTotalKcal();
    if (remainingKcal <= 0) {
      return null;   // 오늘 칼로리를 다 채움
    }

    // 1) 영양소별 부족량
    double carbohydrateLack = TARGET_CARBOHYDRATE - summary.getTotalCarbohydrate();
    double proteinLack = TARGET_PROTEIN - summary.getTotalProtein();
    double fatLack = TARGET_FAT - summary.getTotalFat();

    // 2) 목표 대비 비율로 비교
    double carbohydrateRatio = carbohydrateLack / TARGET_CARBOHYDRATE;
    double proteinRatio = proteinLack / TARGET_PROTEIN;
    double fatRatio = fatLack / TARGET_FAT;

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
      return null;   // 모든 영양소 목표 달성
    }

    // 3) 그 영양소가 가장 많은 음식 찾기
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

    // 4) 부족분을 채우는 양, 남은 칼로리를 넘으면 줄이기
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
