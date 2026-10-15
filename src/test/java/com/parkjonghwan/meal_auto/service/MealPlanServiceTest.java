package com.parkjonghwan.meal_auto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.parkjonghwan.meal_auto.config.SeedFoodCatalog;
import com.parkjonghwan.meal_auto.config.TimeConfig;
import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.FoodCategory;
import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.MealType;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.dto.MealPlan;
import com.parkjonghwan.meal_auto.dto.MealPlanItem;
import com.parkjonghwan.meal_auto.dto.MealPlanResponse;
import com.parkjonghwan.meal_auto.dto.Nutrition;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

  @Mock FoodRepository foodRepository;
  @Mock MealEntryRepository mealEntryRepository;
  @Mock MealSummaryService summaryService;

  private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
  private final Member member = new Member("tester", "encoded");   // 기본 목표: 2000kcal / 300 / 60 / 50
  private final List<Food> menu = new ArrayList<>();

  @BeforeEach
  void setUp() {
    // 실제 기본 음식 목록(seed/foods.csv)으로 메뉴를 만듦
    AtomicLong id = new AtomicLong(1);
    for (var seed : SeedFoodCatalog.foods()) {
      Food food = new Food(seed.name(), seed.kcalPer100g(), seed.carbohydrate(), seed.protein(), seed.fat())
          .withServingGrams(seed.servingGrams()).withCategory(seed.category());
      ReflectionTestUtils.setField(food, "id", id.getAndIncrement());
      menu.add(food);
    }
    lenient().when(foodRepository.findVisibleTo(member)).thenReturn(menu);
    lenient().when(mealEntryRepository.findFoodIdsEatenBetween(any(), any(), any())).thenReturn(List.of());
    lenient().when(mealEntryRepository.findByMemberAndDate(member, DAY)).thenReturn(List.of());
    lenient().when(summaryService.summarizeDay(member, DAY)).thenReturn(new DailySummary(DAY, 0, 0, 0, 0, 0));
  }

  private MealPlanService serviceAt(int hour) {
    Clock clock = Clock.fixed(ZonedDateTime.of(DAY.atTime(hour, 0), TimeConfig.ZONE).toInstant(), TimeConfig.ZONE);
    return new MealPlanService(foodRepository, mealEntryRepository, summaryService, clock);
  }

  private static Nutrition sum(List<MealPlan> plans) {
    return plans.stream().map(MealPlan::total).reduce(Nutrition.ZERO, Nutrition::plus);
  }

  @Test
  void 아침_전이면_아침_점심_저녁을_한식_상차림으로_추천한다() {
    MealPlanResponse result = serviceAt(8).plan(member, DAY);

    assertThat(result.meals()).extracting(MealPlan::mealType)
        .containsExactly(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER);
    for (MealPlan plan : result.meals()) {
      List<FoodCategory> categories = plan.items().stream().map(MealPlanItem::category).toList();
      assertThat(categories).contains(FoodCategory.RICE, FoodCategory.SOUP, FoodCategory.MAIN, FoodCategory.KIMCHI);
      if (plan.mealType() != MealType.BREAKFAST) {
        assertThat(categories).contains(FoodCategory.SIDE);   // 점심·저녁은 나물·볶음까지
      }
    }
  }

  @Test
  void 같은_날_국_주반찬_나물이_겹치지_않는다() {
    MealPlanResponse result = serviceAt(8).plan(member, DAY);

    List<String> dishes = result.meals().stream()
        .flatMap(p -> p.items().stream())
        .filter(i -> i.category() == FoodCategory.SOUP || i.category() == FoodCategory.MAIN
            || i.category() == FoodCategory.SIDE)
        .map(i -> i.food().getName())
        .toList();
    assertThat(dishes).doesNotHaveDuplicates();
  }

  @Test
  void 세_끼를_합치면_하루_목표에_가깝다() {
    MealPlanResponse result = serviceAt(8).plan(member, DAY);
    Nutrition total = sum(result.meals());
    result.meals().forEach(p -> System.out.println(p.mealType() + " 목표 " + p.target() + "\n  합계 " + p.total()
        + "\n  " + p.items().stream().map(i -> i.food().getName() + " " + i.grams() + "g").toList()));
    System.out.println("하루 합계 " + total);

    assertThat(total.kcal()).isBetween(2000 * 0.8, 2000 * 1.15);
    assertThat(total.carbohydrate()).isBetween(300 * 0.7, 300 * 1.15);
    assertThat(total.protein()).isBetween(60 * 0.8, 60 * 1.6);
    assertThat(total.fat()).isBetween(50 * 0.7, 50 * 1.4);
  }

  @Test
  void 점심을_먹었으면_남은_양으로_저녁만_추천한다() {
    // 오후 1시, 점심을 기록함 → 아침은 지났고 점심은 먹었으니 저녁만
    MealEntry lunch = new MealEntry(member, DAY, MealType.LUNCH, menu.get(0), 210);
    when(mealEntryRepository.findByMemberAndDate(member, DAY)).thenReturn(List.of(lunch));
    when(summaryService.summarizeDay(member, DAY)).thenReturn(new DailySummary(DAY, 1, 1300, 180, 40, 35));

    MealPlanResponse result = serviceAt(13).plan(member, DAY);

    assertThat(result.meals()).extracting(MealPlan::mealType).containsExactly(MealType.DINNER);
    MealPlan dinner = result.meals().get(0);
    assertThat(dinner.target().kcal()).isEqualTo(700.0);       // 남은 2000 - 1300 전부
    assertThat(dinner.total().kcal()).isBetween(700 * 0.75, 700 * 1.2);
  }

  @Test
  void 저녁_시간이_지나면_추천하지_않는다() {
    MealPlanResponse result = serviceAt(22).plan(member, DAY);

    assertThat(result.meals()).isEmpty();
    assertThat(result.message()).contains("남은 끼니가 없어요");
  }

  @Test
  void 목표_칼로리를_다_채웠으면_추천하지_않는다() {
    when(summaryService.summarizeDay(member, DAY)).thenReturn(new DailySummary(DAY, 3, 1950, 300, 60, 50));

    MealPlanResponse result = serviceAt(8).plan(member, DAY);

    assertThat(result.meals()).isEmpty();
    assertThat(result.message()).contains("거의 다 채웠어요");
  }

  @Test
  void 최근에_먹은_메뉴는_되도록_피한다() {
    MealPlanResponse first = serviceAt(8).plan(member, DAY);
    Food lunchMain = first.meals().get(1).items().stream()
        .filter(i -> i.category() == FoodCategory.MAIN).findFirst().orElseThrow().food();

    when(mealEntryRepository.findFoodIdsEatenBetween(any(), any(), any())).thenReturn(List.of(lunchMain.getId()));
    MealPlanResponse second = serviceAt(8).plan(member, DAY);

    assertThat(second.meals().get(1).items()).extracting(i -> i.food().getName())
        .doesNotContain(lunchMain.getName());
  }

  @Test
  void 놓친_끼니를_한_끼에_몰아서_추천하지_않는다() {
    // 오후 4시, 오늘 아무것도 안 먹음 → 저녁만 남았지만 하루치 2000kcal를 다 넣지 않음
    MealPlanResponse result = serviceAt(16).plan(member, DAY);

    MealPlan dinner = result.meals().get(0);
    assertThat(dinner.mealType()).isEqualTo(MealType.DINNER);
    assertThat(dinner.target().kcal()).isEqualTo(875.0);   // 2000 × 0.35 × 1.25
    assertThat(dinner.total().kcal()).isLessThan(1000);
  }
}
