package com.parkjonghwan.meal_auto.service;

import com.parkjonghwan.meal_auto.config.TimeConfig;
import com.parkjonghwan.meal_auto.domain.DailyReport;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.dto.Recommendation;
import com.parkjonghwan.meal_auto.repository.DailyReportRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DailyReportService {
  private final DailyReportRepository dailyReportRepository;

  public DailyReportService(DailyReportRepository dailyReportRepository) {
    this.dailyReportRepository = dailyReportRepository;
  }

  // 이미 그 날 리포트가 있으면 새로 만들지 않고 내용만 갱신
  @Transactional
  public DailyReport generateAndSave(Member member, LocalDate date, DailySummary summary,
                                      Recommendation recommendation) {
    var recommendedFood = recommendation == null ? null : recommendation.getFood();
    Double recommendedGrams = recommendation == null ? null : recommendation.getGrams();

    DailyReport report = dailyReportRepository.findByMemberAndDate(member, date)
        .orElse(null);
    if (report != null) {
      report.updateFrom(summary.getMealCount(), summary.getTotalKcal(),
          summary.getTotalCarbohydrate(), summary.getTotalProtein(), summary.getTotalFat(),
          recommendedFood, recommendedGrams);
      return dailyReportRepository.save(report);
    }

    LocalDateTime createdAt = LocalDateTime.now(TimeConfig.ZONE);
    return dailyReportRepository.save(new DailyReport(member, date, summary.getMealCount(),
        summary.getTotalKcal(), summary.getTotalCarbohydrate(), summary.getTotalProtein(),
        summary.getTotalFat(), recommendedFood, recommendedGrams, createdAt));
  }

  @Transactional(readOnly = true)
  public List<DailyReport> getReports(Member member, LocalDate from, LocalDate to) {
    return dailyReportRepository.findByMemberAndDateBetweenOrderByDateAsc(member, from, to);
  }
}
