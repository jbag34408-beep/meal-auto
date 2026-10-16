package com.parkjonghwan.meal_auto.service;

import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.dto.Nutrition;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MealSummaryService {
  private final MealEntryRepository mealEntryRepository;

  public MealSummaryService(MealEntryRepository mealEntryRepository) {
    this.mealEntryRepository = mealEntryRepository;
  }

  @Transactional(readOnly = true)
  public DailySummary summarizeDay(Member member, LocalDate date) {
    List<MealEntry> entries = mealEntryRepository.findByMemberAndDate(member, date);
    Nutrition total = entries.stream()
        .map(entry -> Nutrition.of(entry.getFood(), entry.getGrams()))
        .reduce(Nutrition.ZERO, Nutrition::plus)
        .rounded();
    return new DailySummary(date, entries.size(),
        total.kcal(), total.carbohydrate(), total.protein(), total.fat());
  }
}
