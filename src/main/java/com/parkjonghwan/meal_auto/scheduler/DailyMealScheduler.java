package com.parkjonghwan.meal_auto.scheduler;

import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.dto.Recommendation;
import com.parkjonghwan.meal_auto.service.MealRecommendService;
import com.parkjonghwan.meal_auto.service.MealSummaryService;
import java.time.LocalDate;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyMealScheduler {
  private static final Logger log = LoggerFactory.getLogger(DailyMealScheduler.class);

  private final MealSummaryService summaryService;
  private final MealRecommendService recommendService;

  public DailyMealScheduler(MealSummaryService summaryService,
                            MealRecommendService recommendService) {
    this.summaryService = summaryService;
    this.recommendService = recommendService;
  }

  // 매일 21:00 (한국 시간). 테스트할 때는 "*/10 * * * * *" (10초마다)
  @Scheduled(cron = "0 0 21 * * *", zone = "Asia/Seoul")
  public void dailyReport() {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

    DailySummary summary = summaryService.summarizeDay(today);
    log.info("[{} 요약] 식사 {}회, {} kcal, 탄수화물 {}g, 단백질 {}g, 지방 {}g",
        today, summary.getMealCount(), summary.getTotalKcal(),
        summary.getTotalCarbohydrate(), summary.getTotalProtein(), summary.getTotalFat());

    Recommendation recommendation = recommendService.recommend(summary);
    if (recommendation == null) {
      log.info("[추천] 오늘 목표를 모두 채웠어요");
    } else {
      log.info("[추천] {} {}g - {}", recommendation.getFood().getName(),
          recommendation.getGrams(), recommendation.getReason());
    }
  }
}
