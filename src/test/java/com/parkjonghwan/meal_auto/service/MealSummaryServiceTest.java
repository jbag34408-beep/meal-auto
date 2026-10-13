package com.parkjonghwan.meal_auto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.MealType;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)   // @Mock을 쓰기 위한 설정
class MealSummaryServiceTest {

  @Mock
  private MealEntryRepository mealEntryRepository;   // 가짜 DB

  private MealSummaryService summaryService;
  private final LocalDate today = LocalDate.of(2026, 10, 13);

  @BeforeEach   // 각 테스트 실행 전마다 호출
  void setUp() {
    summaryService = new MealSummaryService(new NutritionCalculator(), mealEntryRepository);
  }

  @Test
  void 하루_세끼를_합산한다() {
    // given: 가짜 DB가 오늘 기록 3개를 돌려주도록 설정
    Food rice = new Food("쌀밥", 200, 45, 4, 0.5);
    Food chicken = new Food("닭가슴살", 110, 0, 23, 1);
    Food banana = new Food("바나나", 90, 23, 1, 0.3);
    when(mealEntryRepository.findByDate(today)).thenReturn(List.of(
        new MealEntry(today, MealType.BREAKFAST, banana, 120),
        new MealEntry(today, MealType.LUNCH, rice, 150),
        new MealEntry(today, MealType.DINNER, chicken, 200)));

    // when
    DailySummary summary = summaryService.summarizeDay(today);

    // then
    assertEquals(3, summary.getMealCount());
    assertEquals(628.0, summary.getTotalKcal(), 0.001);
    assertEquals(95.1, summary.getTotalCarbohydrate(), 0.001);
    assertEquals(53.2, summary.getTotalProtein(), 0.001);
    assertEquals(3.1, summary.getTotalFat(), 0.001);
  }

  @Test
  void 기록이_없으면_전부_0() {
    when(mealEntryRepository.findByDate(today)).thenReturn(List.of());

    DailySummary summary = summaryService.summarizeDay(today);

    assertEquals(0, summary.getMealCount());
    assertEquals(0.0, summary.getTotalKcal(), 0.001);
  }
}