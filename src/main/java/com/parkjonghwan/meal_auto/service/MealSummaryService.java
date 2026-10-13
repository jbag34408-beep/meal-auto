package com.parkjonghwan.meal_auto.service;

import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MealSummaryService {
  private final NutritionCalculator calculator;
  private final MealEntryRepository mealEntryRepository;

  public MealSummaryService(NutritionCalculator calculator,
                            MealEntryRepository mealEntryRepository) {
    this.calculator = calculator;
    this.mealEntryRepository = mealEntryRepository;
  }

  @Transactional(readOnly = true)
  public DailySummary summarizeDay(Member member, LocalDate date) {
    List<MealEntry> entries = mealEntryRepository.findByMemberAndDate(member, date);

    double kcal = 0, carbohydrate = 0, protein = 0, fat = 0;
    for (MealEntry entry : entries) {
      kcal += calculator.calculateKcal(entry);
      carbohydrate += calculator.calculateCarbohydrate(entry);
      protein += calculator.calculateProtein(entry);
      fat += calculator.calculateFat(entry);
    }

    return new DailySummary(date, entries.size(),
        round1(kcal), round1(carbohydrate), round1(protein), round1(fat));
  }

  private double round1(double value) {
    return Math.round(value * 10) / 10.0;
  }
}