package com.parkjonghwan.meal_auto.scheduler;

import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.dto.Recommendation;
import com.parkjonghwan.meal_auto.repository.MemberRepository;
import com.parkjonghwan.meal_auto.service.DailyReportService;
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
  private final DailyReportService reportService;
  private final MemberRepository memberRepository;

  public DailyMealScheduler(MealSummaryService summaryService,
                            MealRecommendService recommendService,
                            DailyReportService reportService,
                            MemberRepository memberRepository) {
    this.summaryService = summaryService;
    this.recommendService = recommendService;
    this.reportService = reportService;
    this.memberRepository = memberRepository;
  }

  // 매일 21:00 (한국 시간), 회원마다 리포트 생성
  @Scheduled(cron = "0 0 21 * * *", zone = "Asia/Seoul")
  public void dailyReport() {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

    for (Member member : memberRepository.findAll()) {
      DailySummary summary = summaryService.summarizeDay(member, today);
      log.info("[{} / {} 요약] 식사 {}회, {} / {} kcal, 탄수화물 {}g, 단백질 {}g, 지방 {}g",
          member.getUsername(), today, summary.getMealCount(),
          summary.getTotalKcal(), member.getTargetKcal(),
          summary.getTotalCarbohydrate(), summary.getTotalProtein(), summary.getTotalFat());

      Recommendation recommendation = recommendService.recommend(summary, member);
      if (recommendation == null) {
        log.info("[{} 추천] 오늘 목표를 모두 채웠어요", member.getUsername());
      } else {
        log.info("[{} 추천] {} {}g - {}", member.getUsername(),
            recommendation.getFood().getName(), recommendation.getGrams(),
            recommendation.getReason());
      }

      reportService.generateAndSave(member, today, summary, recommendation);
    }
  }
}