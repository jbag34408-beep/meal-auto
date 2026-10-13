package com.parkjonghwan.meal_auto.dto;

import java.time.LocalDate;

public class DailySummary {
  private LocalDate date;
  private int mealCount;
  private double totalKcal;
  private double totalCarbohydrate;
  private double totalProtein;
  private double totalFat;

  public DailySummary(LocalDate date, int mealCount, double totalKcal,
                      double totalCarbohydrate, double totalProtein, double totalFat) {
    this.date = date;
    this.mealCount = mealCount;
    this.totalKcal = totalKcal;
    this.totalCarbohydrate = totalCarbohydrate;
    this.totalProtein = totalProtein;
    this.totalFat = totalFat;
  }

  public LocalDate getDate() { return date; }
  public int getMealCount() { return mealCount; }
  public double getTotalKcal() { return totalKcal; }
  public double getTotalCarbohydrate() { return totalCarbohydrate; }
  public double getTotalProtein() { return totalProtein; }
  public double getTotalFat() { return totalFat; }
}
