package com.parkjonghwan.meal_auto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.MealType;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MealSummaryServiceTest {

  @Mock
  private MealEntryRepository mealEntryRepository;

  private MealSummaryService summaryService;
  private final LocalDate today = LocalDate.of(2026, 10, 13);
  private final Member member = new Member("tester", "encoded");

  @BeforeEach
  void setUp() {
    summaryService = new MealSummaryService(mealEntryRepository);
  }

  @Test
  void 하루_세끼를_합산한다() {
    Food rice = new Food("쌀밥", 200, 45, 4, 0.5);
    Food chicken = new Food("닭가슴살", 110, 0, 23, 1);
    Food banana = new Food("바나나", 90, 23, 1, 0.3);
    when(mealEntryRepository.findByMemberAndDate(member, today)).thenReturn(List.of(
        new MealEntry(member, today, MealType.BREAKFAST, banana, 120),
        new MealEntry(member, today, MealType.LUNCH, rice, 150),
        new MealEntry(member, today, MealType.DINNER, chicken, 200)));

    DailySummary summary = summaryService.summarizeDay(member, today);

    assertEquals(3, summary.mealCount());
    assertEquals(628.0, summary.totalKcal(), 0.001);
    assertEquals(95.1, summary.totalCarbohydrate(), 0.001);
    assertEquals(53.2, summary.totalProtein(), 0.001);
    assertEquals(3.1, summary.totalFat(), 0.001);
  }

  @Test
  void 기록이_없으면_전부_0() {
    when(mealEntryRepository.findByMemberAndDate(member, today)).thenReturn(List.of());

    DailySummary summary = summaryService.summarizeDay(member, today);

    assertEquals(0, summary.mealCount());
    assertEquals(0.0, summary.totalKcal(), 0.001);
  }
}