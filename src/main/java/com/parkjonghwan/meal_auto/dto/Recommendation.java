package com.parkjonghwan.meal_auto.dto;

import com.parkjonghwan.meal_auto.domain.Food;

public class Recommendation {
  private Food food;
  private double grams;
  private String reason;

  public Recommendation(Food food, double grams, String reason) {
    this.food = food;
    this.grams = grams;
    this.reason = reason;
  }

  public Food getFood() { return food; }
  public double getGrams() { return grams; }
  public String getReason() { return reason; }
}
