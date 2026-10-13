package com.parkjonghwan.meal_auto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.Member;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MealRecommendServiceTest {

  @Mock
  private FoodRepository foodRepository;

  @InjectMocks
  private MealRecommendService recommendService;

  private final LocalDate today = LocalDate.of(2026, 10, 13);
  private final Food rice = new Food("쌀밥", 200, 45, 4, 0.5);
  private final Food chicken = new Food("닭가슴살", 110, 0, 23, 1);
  private final Food almond = new Food("아몬드", 579, 22, 21, 50);
  private Member member;   // 기본 목표: 2000kcal / 300 / 60 / 50

  @BeforeEach
  void setUp() {
    member = new Member("tester", "encoded");
    Mockito.lenient()
        .when(foodRepository.findAll()).thenReturn(List.of(rice, chicken, almond));
  }

  @Test
  void 지방이_가장_부족하면_지방이_많은_음식을_추천() {
    DailySummary summary = new DailySummary(today, 3, 628.0, 95.1, 53.2, 3.1);

    Recommendation result = recommendService.recommend(summary, member);

    assertEquals("아몬드", result.getFood().getName());
    assertEquals(94.0, result.getGrams(), 0.001);
  }

  @Test
  void 남은_칼로리를_넘지_않게_양을_줄인다() {
    DailySummary summary = new DailySummary(today, 4, 1900.0, 300.0, 60.0, 0.0);

    Recommendation result = recommendService.recommend(summary, member);

    assertEquals("아몬드", result.getFood().getName());
    assertEquals(17.0, result.getGrams(), 0.001);
  }

  @Test
  void 칼로리를_다_채웠으면_추천하지_않는다() {
    DailySummary summary = new DailySummary(today, 5, 2100.0, 300.0, 60.0, 50.0);

    assertNull(recommendService.recommend(summary, member));
  }

  @Test
  void 단백질이_가장_부족하면_단백질이_많은_음식을_추천() {
    DailySummary summary = new DailySummary(today, 2, 1000.0, 300.0, 0.0, 50.0);

    Recommendation result = recommendService.recommend(summary, member);

    assertEquals("닭가슴살", result.getFood().getName());
    assertEquals(261.0, result.getGrams(), 0.001);
  }

  @Test
  void 회원의_목표가_바뀌면_추천도_바뀐다() {
    // 목표 칼로리를 700으로 낮추면 628kcal 먹은 상태에서 남은 칼로리는 72kcal뿐
    member.updateTargets(700, 300, 60, 50);
    DailySummary summary = new DailySummary(today, 3, 628.0, 95.1, 53.2, 3.1);

    Recommendation result = recommendService.recommend(summary, member);

    // 72kcal ÷ 아몬드 579kcal × 100 = 12.4 → 12g (지방 부족분 94g보다 적게)
    assertEquals("아몬드", result.getFood().getName());
    assertEquals(12.0, result.getGrams(), 0.001);
  }
}