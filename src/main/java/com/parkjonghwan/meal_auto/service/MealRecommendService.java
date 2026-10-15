package com.parkjonghwan.meal_auto.service;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.FoodSource;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.dto.NutrientEffect;
import com.parkjonghwan.meal_auto.dto.Recommendation;
import com.parkjonghwan.meal_auto.dto.RecommendationOption;
import com.parkjonghwan.meal_auto.dto.Shortfall;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

// 다음 끼니 추천
// 1) 목표 대비 가장 부족한 영양소를 찾고
// 2) 후보 음식마다 "그 부족분을 채우는 양"을 먹었을 때의 점수를 매겨
//    (양은 한 끼에 알맞게: 최대 500g, 하루 목표 칼로리의 40%, 남은 칼로리 중 작은 값까지)
// 3) 점수 순으로 최대 3개를 돌려줌 (첫 번째가 대표 추천)
//
// 점수 = 채운 정도 - 초과한 정도 - 칼로리 부담 - 최근에 먹은 음식
//   채운 정도:   가장 부족한 영양소의 부족분 중 채운 비율 (0~1)
//              + 다른 영양소의 부족분 중 채운 비율 × 0.25 → 함께 채우면 조금 더 유리
//   초과한 정도: 영양소마다 (목표를 넘긴 g ÷ 목표 g) 의 합 → 이미 채운 영양소를 더 먹게 하면 깎임
//   칼로리 부담: (이 양의 kcal ÷ 남은 kcal) × 0.3 → 같은 효과면 칼로리가 적은 음식이 유리
//   최근 음식:   최근 3일 안에 먹었으면 0.25 → 매번 같은 음식만 나오지 않게
@Service
public class MealRecommendService {
  static final int MAX_OPTIONS = 3;
  static final int RECENT_DAYS = 3;          // 오늘 포함 3일
  static final double OTHER_FILL_WEIGHT = 0.25;
  static final double KCAL_WEIGHT = 0.3;
  static final double RECENT_PENALTY = 0.25;
  static final double MIN_LACK = 1.0;        // 1g 미만 부족은 채울 필요 없음으로 봄
  static final double MAX_PORTION_GRAMS = 500;   // 한 끼에 먹을 수 있는 양
  static final double MEAL_KCAL_SHARE = 0.4;     // 한 끼 칼로리 상한 = 하루 목표의 40%

  private static final String[] NUTRIENTS = {"탄수화물", "단백질", "지방"};

  private final FoodRepository foodRepository;
  private final MealEntryRepository mealEntryRepository;

  public MealRecommendService(FoodRepository foodRepository, MealEntryRepository mealEntryRepository) {
    this.foodRepository = foodRepository;
    this.mealEntryRepository = mealEntryRepository;
  }

  // 고정 상수 대신 회원 각자의 목표로 계산
  public Recommendation recommend(DailySummary summary, Member member) {
    double remainingKcal = member.getTargetKcal() - summary.getTotalKcal();
    if (remainingKcal <= 0) {
      return null;
    }

    double[] target = {member.getTargetCarbohydrate(), member.getTargetProtein(), member.getTargetFat()};
    double[] eaten = {summary.getTotalCarbohydrate(), summary.getTotalProtein(), summary.getTotalFat()};
    double[] lack = new double[3];
    double[] ratio = new double[3];
    List<Shortfall> shortfalls = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      double rawLack = target[i] - eaten[i];
      ratio[i] = rawLack / target[i];
      lack[i] = rawLack >= MIN_LACK ? rawLack : 0;
      shortfalls.add(new Shortfall(NUTRIENTS[i], target[i], round1(eaten[i]), round1(rawLack),
          Math.round(ratio[i] * 1000) / 1000.0));
    }

    // 가장 부족한 영양소 (비율 기준, 같으면 탄수화물 → 단백질 → 지방 순)
    int primary = 0;
    for (int i = 1; i < 3; i++) {
      if (ratio[i] > ratio[primary]) {
        primary = i;
      }
    }
    if (lack[primary] <= 0) {
      return null;
    }

    // 후보: 공용 음식 + 내 음식만 (다른 회원이 등록하거나 AI로 만든 음식은 제외)
    List<Food> candidates = foodRepository.findVisibleTo(member).stream()
        .filter(MealRecommendService::isTrustedFor)
        .toList();
    Set<Long> recentFoodIds = new HashSet<>(mealEntryRepository.findFoodIdsEatenBetween(
        member, summary.getDate().minusDays(RECENT_DAYS - 1), summary.getDate()));

    double mealKcal = Math.min(remainingKcal, member.getTargetKcal() * MEAL_KCAL_SHARE);
    List<Scored> scored = new ArrayList<>();
    for (Food food : candidates) {
      Scored s = score(food, primary, target, lack, remainingKcal, mealKcal,
          food.getId() != null && recentFoodIds.contains(food.getId()));
      if (s != null) {
        scored.add(s);
      }
    }
    if (scored.isEmpty()) {
      return null;
    }
    scored.sort(Comparator.comparingDouble((Scored s) -> s.option.score()).reversed());

    List<RecommendationOption> options = scored.stream().limit(MAX_OPTIONS).map(s -> s.option).toList();
    Scored best = scored.get(0);
    String reason = String.format("%s이 목표보다 %.1fg 부족해요", NUTRIENTS[primary], lack[primary]);
    return new Recommendation(best.option.food(), best.option.grams(), reason, NUTRIENTS[primary],
        best.neededGrams, best.option.kcal(), round1(remainingKcal), best.option.capped(),
        shortfalls, options);
  }

  private record Scored(RecommendationOption option, double neededGrams) {}

  // 가장 부족한 영양소를 다 채우는 양을 먹었을 때의 점수
  // 그 양이 한 끼 상한(500g, 한 끼 칼로리)을 넘으면 상한까지 줄임
  private Scored score(Food food, int primary, double[] target, double[] lack,
                       double remainingKcal, double mealKcal, boolean recent) {
    double[] per100 = {food.getCarbohydrate(), food.getProtein(), food.getFat()};
    if (per100[primary] <= 0) {
      return null;   // 부족한 영양소가 없는 음식은 후보가 아님
    }

    double grams = lack[primary] / per100[primary] * 100;
    double neededGrams = Math.round(grams);
    double maxGrams = MAX_PORTION_GRAMS;
    if (food.getKcalPer100g() > 0) {
      maxGrams = Math.min(maxGrams, mealKcal / food.getKcalPer100g() * 100);
    }
    boolean capped = grams > maxGrams;
    if (capped) {
      grams = maxGrams;
    }
    grams = Math.round(grams);
    if (grams < 1) {   // 1g도 안 되는 양은 추천하지 않음 (기록 최소량도 1g)
      return null;
    }

    double kcal = food.getKcalPer100g() * grams / 100;
    double fillScore = 0;
    double overPenalty = 0;
    List<NutrientEffect> effects = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      double added = per100[i] * grams / 100;
      double filled = Math.min(added, lack[i]);
      double over = added - filled;
      if (lack[i] > 0) {
        fillScore += (i == primary ? 1 : OTHER_FILL_WEIGHT) * filled / lack[i];
      }
      overPenalty += over / target[i];
      effects.add(new NutrientEffect(NUTRIENTS[i], round1(added), round1(filled), round1(over)));
    }
    double kcalPenalty = KCAL_WEIGHT * kcal / remainingKcal;
    double recentPenalty = recent ? RECENT_PENALTY : 0;
    double score = fillScore - overPenalty - kcalPenalty - recentPenalty;

    RecommendationOption option = new RecommendationOption(food, grams, round1(kcal), capped, recent,
        effects, round3(score), round3(fillScore), round3(overPenalty), round3(kcalPenalty),
        recentPenalty, summarize(effects, recent));
    return new Scored(option, neededGrams);
  }

  // 예: "지방 46.9g · 단백질 6.8g 채움, 단백질 12.9g 초과"
  private static String summarize(List<NutrientEffect> effects, boolean recent) {
    List<String> filled = effects.stream()
        .filter(e -> e.filled() >= 0.1)
        .sorted(Comparator.comparingDouble(NutrientEffect::filled).reversed())
        .map(e -> String.format("%s %.1fg", e.nutrient(), e.filled()))
        .toList();
    List<String> over = effects.stream()
        .filter(e -> e.over() >= 0.5)   // 그램 반올림으로 생기는 아주 작은 초과는 빼고 보여 줌
        .map(e -> String.format("%s %.1fg", e.nutrient(), e.over()))
        .toList();

    StringBuilder text = new StringBuilder(String.join(" · ", filled)).append(" 채움");
    if (!over.isEmpty()) {
      text.append(", ").append(String.join(" · ", over)).append(" 초과");
    }
    if (recent) {
      text.append(" (최근에 먹은 음식)");
    }
    return text.toString();
  }

  // 주인 기능 이전에 만들어져 주인을 알 수 없는 AI 음식이 공용으로 남아 있을 수 있음
  // → 공용인데 AI 출처면 값을 확인할 수 없으므로 추천하지 않음 (내 AI 음식은 내 추천에 써도 됨)
  private static boolean isTrustedFor(Food food) {
    return !(food.isShared() && food.getSource() == FoodSource.AI);
  }

  private static double round1(double value) {
    return Math.round(value * 10) / 10.0;
  }

  private static double round3(double value) {
    return Math.round(value * 1000) / 1000.0;
  }
}
