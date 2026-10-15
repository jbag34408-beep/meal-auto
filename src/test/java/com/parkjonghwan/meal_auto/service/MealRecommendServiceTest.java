package com.parkjonghwan.meal_auto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.FoodSource;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.dto.Recommendation;
import com.parkjonghwan.meal_auto.dto.RecommendationOption;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MealRecommendServiceTest {

  @Mock
  private FoodRepository foodRepository;

  @Mock
  private MealEntryRepository mealEntryRepository;

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
        .when(foodRepository.findVisibleTo(member)).thenReturn(List.of(rice, chicken, almond));
    Mockito.lenient()   // 기본: 최근에 먹은 음식 없음
        .when(mealEntryRepository.findFoodIdsEatenBetween(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(List.of());
  }

  @Test
  void 지방이_가장_부족하면_지방이_많은_음식을_추천() {
    DailySummary summary = new DailySummary(today, 3, 628.0, 95.1, 53.2, 3.1);

    Recommendation result = recommendService.recommend(summary, member);

    assertEquals("아몬드", result.getFood().getName());
    assertEquals(94.0, result.getGrams(), 0.001);
  }

  @Test
  void 추천_근거를_함께_돌려준다() {
    DailySummary summary = new DailySummary(today, 3, 628.0, 95.1, 53.2, 3.1);

    Recommendation result = recommendService.recommend(summary, member);

    // 부족 비율: 탄수화물 204.9/300=0.683, 단백질 6.8/60=0.113, 지방 46.9/50=0.938 → 지방
    assertEquals("지방", result.getNutrient());
    assertEquals(3, result.getShortfalls().size());
    assertEquals(0.683, result.getShortfalls().get(0).ratio(), 0.0001);
    assertEquals(0.113, result.getShortfalls().get(1).ratio(), 0.0001);
    assertEquals(46.9, result.getShortfalls().get(2).lack(), 0.0001);
    assertEquals(0.938, result.getShortfalls().get(2).ratio(), 0.0001);
    assertEquals(94.0, result.getNeededGrams(), 0.001);
    assertEquals(544.3, result.getKcal(), 0.001);
    assertEquals(1372.0, result.getRemainingKcal(), 0.001);
    assertFalse(result.isCapped());
  }

  @Test
  void 남은_칼로리를_넘지_않게_양을_줄인다() {
    DailySummary summary = new DailySummary(today, 4, 1900.0, 300.0, 60.0, 0.0);

    Recommendation result = recommendService.recommend(summary, member);

    assertEquals("아몬드", result.getFood().getName());
    assertEquals(17.0, result.getGrams(), 0.001);
    // 지방 50g을 채우려면 100g이 필요하지만 남은 100kcal에 맞춰 줄임
    assertTrue(result.isCapped());
    assertEquals(100.0, result.getNeededGrams(), 0.001);
    assertEquals(98.4, result.getKcal(), 0.001);
    assertEquals(100.0, result.getRemainingKcal(), 0.001);
  }

  @Test
  void 칼로리를_다_채웠으면_추천하지_않는다() {
    DailySummary summary = new DailySummary(today, 5, 2100.0, 300.0, 60.0, 50.0);

    assertNull(recommendService.recommend(summary, member));
  }

  @Test
  void 추천량이_1g도_안_되면_추천하지_않는다() {
    // 탄수화물 0.2g 부족 → 쌀밥 0.4g → 반올림하면 0g
    DailySummary summary = new DailySummary(today, 4, 1990.0, 299.8, 60.0, 50.0);

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

  @Test
  void 주인을_모르는_공용_AI_음식은_후보에서_빼고_내_AI_음식은_후보에_넣는다() {
    // 지방이 가장 부족한 상태
    DailySummary summary = new DailySummary(today, 3, 628.0, 95.1, 53.2, 3.1);
    Food sharedAi = new Food("버터(AI)", 717, 0, 1, 81, FoodSource.AI, null);
    Food myAi = new Food("내버터(AI)", 717, 0, 1, 70, FoodSource.AI, member);

    Mockito.when(foodRepository.findVisibleTo(member)).thenReturn(List.of(rice, chicken, almond, sharedAi));
    assertTrue(recommendService.recommend(summary, member).getOptions().stream()
        .noneMatch(o -> o.food() == sharedAi));

    Mockito.when(foodRepository.findVisibleTo(member)).thenReturn(List.of(rice, chicken, almond, myAi));
    assertTrue(recommendService.recommend(summary, member).getOptions().stream()
        .anyMatch(o -> o.food() == myAi));
  }

  @Test
  void 후보를_점수_순으로_최대_3개_돌려주고_첫_번째가_대표_추천이다() {
    DailySummary summary = new DailySummary(today, 3, 628.0, 95.1, 53.2, 3.1);

    Recommendation result = recommendService.recommend(summary, member);

    List<RecommendationOption> options = result.getOptions();
    assertEquals(3, options.size());
    assertEquals(result.getFood(), options.get(0).food());
    assertTrue(options.get(0).score() >= options.get(1).score());
    assertTrue(options.get(1).score() >= options.get(2).score());

    // 아몬드 94g: 지방 46.9g·단백질 6.8g(부족분 전부)·탄수화물 20.7g을 채우고 단백질 12.9g을 넘김
    RecommendationOption almondOption = options.get(0);
    assertEquals(46.9, almondOption.effects().get(2).filled(), 0.001);
    assertEquals(6.8, almondOption.effects().get(1).filled(), 0.001);
    assertEquals(12.9, almondOption.effects().get(1).over(), 0.001);
    assertTrue(almondOption.summary().contains("단백질 12.9g 초과"), almondOption.summary());
  }

  @Test
  void 부족한_영양소가_가장_많아도_다른_영양소를_크게_넘기면_밀린다() {
    // 단백질만 60g 부족, 탄수화물·지방은 이미 다 채움
    DailySummary summary = new DailySummary(today, 2, 1000.0, 300.0, 0.0, 50.0);
    // 단백질 함량은 닭가슴살(23g)보다 높지만 탄수화물·지방도 많은 음식
    Food proteinBar = new Food("프로틴바", 450, 40, 30, 20);
    Mockito.when(foodRepository.findVisibleTo(member)).thenReturn(List.of(proteinBar, chicken));

    Recommendation result = recommendService.recommend(summary, member);

    assertEquals("닭가슴살", result.getFood().getName());
    assertEquals("프로틴바", result.getOptions().get(1).food().getName());
    assertTrue(result.getOptions().get(1).overPenalty() > 0.5);   // 탄수화물·지방 초과로 크게 깎임
  }

  @Test
  void 최근에_먹은_음식은_점수가_깎여_비슷한_다른_음식이_먼저_나온다() {
    DailySummary summary = new DailySummary(today, 2, 1000.0, 300.0, 0.0, 50.0);
    Food fish = new Food("흰살생선", 90, 0, 20, 1);
    ReflectionTestUtils.setField(chicken, "id", 2L);
    ReflectionTestUtils.setField(fish, "id", 9L);
    Mockito.when(foodRepository.findVisibleTo(member)).thenReturn(List.of(chicken, fish));

    // 최근에 안 먹었으면 닭가슴살이 근소하게 앞섬
    assertEquals("닭가슴살", recommendService.recommend(summary, member).getFood().getName());

    // 어제 닭가슴살을 먹었으면 흰살생선이 먼저
    Mockito.when(mealEntryRepository.findFoodIdsEatenBetween(member, today.minusDays(2), today))
        .thenReturn(List.of(2L));
    Recommendation result = recommendService.recommend(summary, member);
    assertEquals("흰살생선", result.getFood().getName());
    RecommendationOption chickenOption = result.getOptions().get(1);
    assertTrue(chickenOption.recent());
    assertTrue(chickenOption.summary().contains("최근에 먹은 음식"));
  }

  @Test
  void 아무것도_안_먹었어도_한_끼에_먹기_어려운_양은_추천하지_않는다() {
    // 탄수화물 300g이 모두 부족 → 쌀밥으로 채우려면 667g(1333kcal)이지만 한 끼 상한에 맞춰 줄임
    DailySummary summary = new DailySummary(today, 0, 0.0, 0.0, 0.0, 0.0);

    Recommendation result = recommendService.recommend(summary, member);

    for (RecommendationOption option : result.getOptions()) {
      assertTrue(option.grams() <= 500, option.food().getName() + " " + option.grams() + "g");
      assertTrue(option.kcal() <= 2000 * 0.4 + 1, option.food().getName() + " " + option.kcal() + "kcal");
    }
    RecommendationOption riceOption = result.getOptions().stream()
        .filter(o -> o.food().getName().equals("쌀밥")).findFirst().orElseThrow();
    assertEquals(400.0, riceOption.grams(), 0.001);   // 800kcal ÷ 200kcal × 100
    assertTrue(riceOption.capped());
  }

  @Test
  void 일인분이_정해진_음식은_최대_2인분까지만_추천한다() {
    // 탄수화물만 크게 부족: 꿀로 채우려면 수백 g이 필요하지만 1인분 20g × 2 = 40g까지만
    DailySummary summary = new DailySummary(today, 1, 300.0, 50.0, 60.0, 50.0);
    Food honey = new Food("꿀", 304, 82.4, 0.3, 0).withServingGrams(20.0);
    Mockito.when(foodRepository.findVisibleTo(member)).thenReturn(List.of(rice, honey));

    Recommendation result = recommendService.recommend(summary, member);

    RecommendationOption honeyOption = result.getOptions().stream()
        .filter(o -> o.food() == honey).findFirst().orElseThrow();
    assertEquals(40.0, honeyOption.grams(), 0.001);
    assertTrue(honeyOption.capped());
    assertEquals("쌀밥", result.getFood().getName());   // 한 끼 양으로 더 많이 채우는 쌀밥이 1위
  }
}
