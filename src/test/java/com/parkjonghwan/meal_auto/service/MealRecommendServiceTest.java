package com.parkjonghwan.meal_auto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.dto.Recommendation;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MealRecommendServiceTest {

  @Mock
  private FoodRepository foodRepository;

  @InjectMocks   // 위의 가짜 foodRepository를 생성자에 넣어서 만들어 줌
  private MealRecommendService recommendService;

  private final LocalDate today = LocalDate.of(2026, 10, 13);
  private final Food rice = new Food("쌀밥", 200, 45, 4, 0.5);
  private final Food chicken = new Food("닭가슴살", 110, 0, 23, 1);
  private final Food almond = new Food("아몬드", 579, 22, 21, 50);

  @BeforeEach
  void setUp() {
    // lenient: 이 설정을 안 쓰는 테스트가 있어도 경고하지 않음
    org.mockito.Mockito.lenient()
        .when(foodRepository.findAll()).thenReturn(List.of(rice, chicken, almond));
  }
  @Test
  void 단백질이_가장_부족하면_단백질이_많은_음식을_추천() {
    // given: 탄수화물·지방은 목표 달성, 단백질만 0g, 칼로리는 1000kcal 남음
    DailySummary summary = new DailySummary(today, 2, 1000.0, 300.0, 0.0, 50.0);

    // when
    Recommendation result = recommendService.recommend(summary);

    // then: 단백질 부족 60g ÷ 닭가슴살 100g당 23g × 100 = 260.87 → 261g
    assertEquals("닭가슴살", result.getFood().getName());
    assertEquals(261.0, result.getGrams(), 0.001);
    assertEquals("단백질이 목표보다 60.0g 부족해요", result.getReason());
  }
 
 
  @Test
  void 지방이_가장_부족하면_지방이_많은_음식을_추천() {
    // given: 앱에서 실제로 나온 요약값
    DailySummary summary = new DailySummary(today, 3, 628.0, 95.1, 53.2, 3.1);

    // when
    Recommendation result = recommendService.recommend(summary);

    // then: 지방 부족 46.9g ÷ 아몬드 100g당 50g × 100 = 93.8 → 94g
    assertEquals("아몬드", result.getFood().getName());
    assertEquals(94.0, result.getGrams(), 0.001);
  }

  @Test
  void 남은_칼로리를_넘지_않게_양을_줄인다() {
    // given: 남은 칼로리 100kcal, 지방만 부족
    DailySummary summary = new DailySummary(today, 4, 1900.0, 300.0, 60.0, 0.0);

    // when
    Recommendation result = recommendService.recommend(summary);

    // then: 아몬드 100g = 579kcal → 100kcal만큼만 = 17.27 → 17g
    assertEquals("아몬드", result.getFood().getName());
    assertEquals(17.0, result.getGrams(), 0.001);
  }

  @Test
  void 칼로리를_다_채웠으면_추천하지_않는다() {
    DailySummary summary = new DailySummary(today, 5, 2100.0, 300.0, 60.0, 50.0);

    assertNull(recommendService.recommend(summary));
  }
}