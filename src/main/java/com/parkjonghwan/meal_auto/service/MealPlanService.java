package com.parkjonghwan.meal_auto.service;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.FoodCategory;
import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.MealType;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.MealPlan;
import com.parkjonghwan.meal_auto.dto.MealPlanItem;
import com.parkjonghwan.meal_auto.dto.MealPlanResponse;
import com.parkjonghwan.meal_auto.dto.Nutrition;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

// 하루 식단 추천: 아직 안 먹은 아침·점심·저녁마다 한식 상차림 하나씩
//
// 1) 오늘 목표에서 이미 먹은 양을 빼서 남은 칼로리·탄수화물·단백질·지방을 구함
// 2) 남은 끼니에 나눔 (아침 25%, 점심 40%, 저녁 35% 비율, 앞 끼니에 고른 상차림만큼 빼고 다음 끼니를 다시 나눔)
//    단, 한 끼 목표는 그 끼니 기본 몫(하루 목표 × 비율)의 1.25배까지 → 놓친 끼니를 한 끼에 몰아 먹게 하지 않음
// 3) 끼니마다 밥 + 국·찌개 + 주반찬 (+ 나물·볶음) + 김치 조합을 모두 맞춰 보고 목표에 가장 가까운 상차림을 고름
//    - 밥은 반 공기 ~ 1과 1/4 공기, 주반찬은 1인분 또는 1.5인분으로 양을 조절
//    - 아침은 나물·볶음 없이 가볍게
//    - 같은 날 다른 끼니와 국·주반찬·나물이 겹치지 않게, 최근 3일 안에 먹은 메뉴는 덜 고르게
@Service
public class MealPlanService {
  private static final List<MealType> MEALS = List.of(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER);
  private static final Map<MealType, Double> WEIGHT = Map.of(
      MealType.BREAKFAST, 0.25, MealType.LUNCH, 0.40, MealType.DINNER, 0.35);

  private static final double[] RICE_PORTIONS = {0.5, 0.75, 1.0, 1.25};   // 공기 단위
  private static final double[] MAIN_PORTIONS = {1.0, 1.5};               // 인분 단위
  private static final double MEAL_CAP = 1.25;   // 한 끼 목표 상한 = 하루 목표 × 끼니 비율 × 1.25
  private static final int RECENT_DAYS = 3;
  private static final double RECENT_PENALTY = 0.4;   // 최근 3일 안에 먹은 국·주반찬·나물 하나당

  // 목표가 아주 작을 때 오차가 지나치게 커지지 않도록 쓰는 최소 기준 (g, kcal)
  private static final double CARB_FLOOR = 30;
  private static final double PROTEIN_FLOOR = 15;
  private static final double FAT_FLOOR = 8;
  private static final double KCAL_FLOOR = 150;
  private static final double OVER_WEIGHT = 1.3;          // 탄수화물·지방·칼로리는 넘는 쪽을 조금 더 나쁘게 봄
  private static final double PROTEIN_OVER_WEIGHT = 0.3;  // 단백질은 목표를 조금 넘어도 괜찮아서 덜 깎음
  private static final double KCAL_WEIGHT = 2.0;          // 끼니 칼로리를 맞추는 게 가장 중요

  private final FoodRepository foodRepository;
  private final MealEntryRepository mealEntryRepository;
  private final MealSummaryService summaryService;
  private final Clock clock;

  public MealPlanService(FoodRepository foodRepository, MealEntryRepository mealEntryRepository,
                         MealSummaryService summaryService, Clock clock) {
    this.foodRepository = foodRepository;
    this.mealEntryRepository = mealEntryRepository;
    this.summaryService = summaryService;
    this.clock = clock;
  }

  public MealPlanResponse plan(Member member, LocalDate date) {
    var summary = summaryService.summarizeDay(member, date);
    Nutrition target = new Nutrition(member.getTargetKcal(), member.getTargetCarbohydrate(),
        member.getTargetProtein(), member.getTargetFat());
    Nutrition remaining = target.minusFloorZero(summary.total());

    List<MealType> open = openMeals(member, date);
    if (open.isEmpty()) {
      return new MealPlanResponse(date, remaining.rounded(), List.of(),
          date.equals(LocalDate.now(clock))
              ? "오늘 남은 끼니가 없어요. 아침·점심·저녁을 모두 기록했거나 저녁 시간이 지났어요."
              : "이날은 아침·점심·저녁을 모두 기록했어요.");
    }
    if (remaining.kcal() < 100) {
      return new MealPlanResponse(date, remaining.rounded(), List.of(),
          "목표 칼로리를 거의 다 채웠어요. 더 먹는다면 가벼운 채소나 과일 정도면 충분해요.");
    }

    Map<FoodCategory, List<Food>> menu = foodRepository.findVisibleTo(member).stream()
        .filter(f -> f.getCategory() != null && f.getServingGrams() != null)
        .collect(Collectors.groupingBy(Food::getCategory, () -> new EnumMap<>(FoodCategory.class),
            Collectors.toList()));
    if (!menu.containsKey(FoodCategory.RICE) || !menu.containsKey(FoodCategory.SOUP)
        || !menu.containsKey(FoodCategory.MAIN)) {
      return new MealPlanResponse(date, remaining.rounded(), List.of(),
          "상차림에 쓸 기본 메뉴(밥·국·주반찬)가 아직 없어요.");
    }
    Set<Long> recent = new HashSet<>(mealEntryRepository.findFoodIdsEatenBetween(
        member, date.minusDays(RECENT_DAYS - 1), date));

    List<MealPlan> plans = new ArrayList<>();
    Set<Long> usedToday = new HashSet<>();
    Nutrition left = remaining;
    double weightLeft = open.stream().mapToDouble(WEIGHT::get).sum();
    for (MealType meal : open) {
      Nutrition share = left.times(WEIGHT.get(meal) / weightLeft);
      Nutrition cap = target.times(WEIGHT.get(meal) * MEAL_CAP);
      Nutrition mealTarget = new Nutrition(Math.min(share.kcal(), cap.kcal()),
          Math.min(share.carbohydrate(), cap.carbohydrate()), Math.min(share.protein(), cap.protein()),
          Math.min(share.fat(), cap.fat()));
      MealPlan plan = bestPlan(meal, mealTarget, menu, recent, usedToday);
      plans.add(plan);
      plan.items().stream()
          .filter(i -> i.category() != FoodCategory.RICE && i.category() != FoodCategory.KIMCHI)
          .forEach(i -> usedToday.add(i.food().getId()));
      left = left.minusFloorZero(plan.total());
      weightLeft -= WEIGHT.get(meal);
    }
    return new MealPlanResponse(date, remaining.rounded(), plans, null);
  }

  // 추천할 끼니: 아직 기록이 없는 아침·점심·저녁 (오늘이면 이미 지난 끼니는 빼고)
  private List<MealType> openMeals(Member member, LocalDate date) {
    Set<MealType> recorded = mealEntryRepository.findByMemberAndDate(member, date).stream()
        .map(MealEntry::getMealType)
        .collect(Collectors.toSet());
    int from = 0;
    if (date.equals(LocalDate.now(clock))) {
      LocalTime now = LocalTime.now(clock);
      if (now.getHour() >= 21) {
        return List.of();
      }
      from = now.getHour() < 10 ? 0 : now.getHour() < 15 ? 1 : 2;
    }
    return MEALS.subList(from, MEALS.size()).stream()
        .filter(m -> !recorded.contains(m))
        .toList();
  }

  private record Pick(Food food, double grams, Nutrition nutrition) {}

  // 상차림 조합을 모두 맞춰 보고 오차가 가장 작은 것
  private MealPlan bestPlan(MealType meal, Nutrition target, Map<FoodCategory, List<Food>> menu,
                            Set<Long> recent, Set<Long> usedToday) {
    List<Pick> rices = picks(menu.get(FoodCategory.RICE), RICE_PORTIONS, Set.of());
    List<Pick> soups = picks(menu.get(FoodCategory.SOUP), new double[] {1.0}, usedToday);
    List<Pick> mains = picks(menu.get(FoodCategory.MAIN), MAIN_PORTIONS, usedToday);
    List<Pick> sides = meal == MealType.BREAKFAST
        ? List.of()
        : picks(menu.getOrDefault(FoodCategory.SIDE, List.of()), new double[] {1.0}, usedToday);
    List<Pick> kimchis = picks(menu.getOrDefault(FoodCategory.KIMCHI, List.of()), new double[] {1.0}, Set.of());
    // 겹치지 않는 메뉴가 바닥나면 겹쳐도 고름
    if (soups.isEmpty()) soups = picks(menu.get(FoodCategory.SOUP), new double[] {1.0}, Set.of());
    if (mains.isEmpty()) mains = picks(menu.get(FoodCategory.MAIN), MAIN_PORTIONS, Set.of());

    List<Pick> sideChoices = sides.isEmpty() ? Collections.singletonList(null) : sides;
    List<Pick> kimchiChoices = kimchis.isEmpty() ? Collections.singletonList(null) : kimchis;

    double bestScore = Double.MAX_VALUE;
    List<Pick> best = null;
    for (Pick rice : rices) {
      for (Pick soup : soups) {
        Nutrition rs = rice.nutrition().plus(soup.nutrition());
        for (Pick main : mains) {
          Nutrition rsm = rs.plus(main.nutrition());
          for (Pick side : sideChoices) {
            Nutrition rsms = side == null ? rsm : rsm.plus(side.nutrition());
            for (Pick kimchi : kimchiChoices) {
              Nutrition total = kimchi == null ? rsms : rsms.plus(kimchi.nutrition());
              double score = error(total, target)
                  + penalty(recent, soup, main, side);
              if (score < bestScore) {
                bestScore = score;
                best = new ArrayList<>();
                best.add(rice);
                best.add(soup);
                best.add(main);
                if (side != null) best.add(side);
                if (kimchi != null) best.add(kimchi);
              }
            }
          }
        }
      }
    }

    Nutrition total = Nutrition.ZERO;
    List<MealPlanItem> items = new ArrayList<>();
    for (Pick p : best) {
      total = total.plus(p.nutrition());
      items.add(new MealPlanItem(p.food(), p.food().getCategory().label(), p.grams(), p.nutrition().rounded(),
          p.food().getId() != null && recent.contains(p.food().getId())));
    }
    return new MealPlan(meal, target.rounded(), total.rounded(), items);
  }

  private static List<Pick> picks(List<Food> foods, double[] portions, Set<Long> exclude) {
    List<Pick> result = new ArrayList<>();
    for (Food food : foods) {
      if (food.getId() != null && exclude.contains(food.getId())) {
        continue;
      }
      for (double portion : portions) {
        double grams = Math.round(food.getServingGrams() * portion);
        result.add(new Pick(food, grams, Nutrition.of(food, grams)));
      }
    }
    return result;
  }

  // 목표와의 차이: 영양소마다 (목표 대비 차이)²
  // 칼로리는 2배로 중요하게, 넘친 쪽은 1.3배 (단백질만 넘쳐도 0.3배)
  static double error(Nutrition actual, Nutrition target) {
    return sq(actual.carbohydrate(), target.carbohydrate(), CARB_FLOOR, OVER_WEIGHT)
        + sq(actual.protein(), target.protein(), PROTEIN_FLOOR, PROTEIN_OVER_WEIGHT)
        + sq(actual.fat(), target.fat(), FAT_FLOOR, OVER_WEIGHT)
        + KCAL_WEIGHT * sq(actual.kcal(), target.kcal(), KCAL_FLOOR, OVER_WEIGHT);
  }

  private static double sq(double actual, double target, double floor, double overWeight) {
    double d = (actual - target) / Math.max(target, floor);
    return d * d * (d > 0 ? overWeight : 1.0);
  }

  private static double penalty(Set<Long> recent, Pick... picks) {
    double p = 0;
    for (Pick pick : picks) {
      if (pick != null && pick.food().getId() != null && recent.contains(pick.food().getId())) {
        p += RECENT_PENALTY;
      }
    }
    return p;
  }
}
