package com.parkjonghwan.meal_auto.dto;

import com.parkjonghwan.meal_auto.domain.Food;
import java.util.List;

public class Recommendation {
  private Food food;
  private double grams;
  private String reason;

  // 화면에서 "왜 이 추천인지" 보여주기 위한 계산 근거
  private String nutrient;              // 가장 부족한 영양소
  private double neededGrams;           // 부족분을 다 채우는 양 (칼로리 상한 적용 전)
  private double kcal;                  // 추천량의 칼로리
  private double remainingKcal;         // 오늘 남은 칼로리
  private boolean capped;               // 한 끼 양(500g·한 끼 칼로리·남은 칼로리)에 맞춰 줄였는지
  private List<Shortfall> shortfalls;   // 탄수화물·단백질·지방 각각의 부족 정도
  private List<RecommendationOption> options;   // 점수 순 후보 (최대 3개, 첫 번째가 위 food)

  public Recommendation(Food food, double grams, String reason, String nutrient,
                        double neededGrams, double kcal, double remainingKcal,
                        boolean capped, List<Shortfall> shortfalls,
                        List<RecommendationOption> options) {
    this.food = food;
    this.grams = grams;
    this.reason = reason;
    this.nutrient = nutrient;
    this.neededGrams = neededGrams;
    this.kcal = kcal;
    this.remainingKcal = remainingKcal;
    this.capped = capped;
    this.shortfalls = shortfalls;
    this.options = options;
  }

  public Food getFood() { return food; }
  public double getGrams() { return grams; }
  public String getReason() { return reason; }
  public String getNutrient() { return nutrient; }
  public double getNeededGrams() { return neededGrams; }
  public double getKcal() { return kcal; }
  public double getRemainingKcal() { return remainingKcal; }
  public boolean isCapped() { return capped; }
  public List<Shortfall> getShortfalls() { return shortfalls; }
  public List<RecommendationOption> getOptions() { return options; }
}
