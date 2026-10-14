package com.parkjonghwan.meal_auto.service;

import com.parkjonghwan.meal_auto.domain.DailyReport;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.dto.Recommendation;
import com.parkjonghwan.meal_auto.exception.InvalidDateRangeException;
import com.parkjonghwan.meal_auto.repository.DailyReportRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DailyReportService {
  static final int MAX_RANGE_DAYS = 31;

  private final DailyReportRepository dailyReportRepository;
  private final MealSummaryService summaryService;
  private final MealRecommendService recommendService;
  private final Clock clock;

  public DailyReportService(DailyReportRepository dailyReportRepository,
                            MealSummaryService summaryService,
                            MealRecommendService recommendService,
                            Clock clock) {
    this.dailyReportRepository = dailyReportRepository;
    this.summaryService = summaryService;
    this.recommendService = recommendService;
    this.clock = clock;
  }

  // 이미 그 날 리포트가 있으면 새로 만들지 않고 내용만 갱신
  @Transactional
  public DailyReport generateAndSave(Member member, LocalDate date, DailySummary summary,
                                      Recommendation recommendation) {
    DailyReport report = dailyReportRepository.findByMemberAndDate(member, date)
        .orElse(null);
    if (report != null) {
      report.updateFrom(summary.getMealCount(), summary.getTotalKcal(),
          summary.getTotalCarbohydrate(), summary.getTotalProtein(), summary.getTotalFat(),
          recommendation == null ? null : recommendation.getFood(),
          recommendation == null ? null : recommendation.getGrams());
      return dailyReportRepository.save(report);
    }
    return dailyReportRepository.save(newReport(member, date, summary, recommendation));
  }

  // 서버가 잠들어 21시 스케줄러가 못 돈 날은 조회할 때 채워 넣음
  // - 오늘(아직 하루가 안 끝남)과 미래는 만들지 않음
  // - 식사 기록이 0건인 날은 저장하지 않고 응답에서도 뺌
  //   (가입 전 날짜에 빈 리포트가 쌓이지 않고, 나중에 기록하면 그때 만들어짐)
  @Transactional
  public List<DailyReport> getReports(Member member, LocalDate from, LocalDate to) {
    validateRange(from, to);

    List<DailyReport> reports = new ArrayList<>(
        dailyReportRepository.findByMemberAndDateBetweenOrderByDateAsc(member, from, to));
    Set<LocalDate> existingDates = reports.stream()
        .map(DailyReport::getDate)
        .collect(Collectors.toSet());

    LocalDate yesterday = LocalDate.now(clock).minusDays(1);
    LocalDate last = to.isBefore(yesterday) ? to : yesterday;
    boolean created = false;
    for (LocalDate date = from; !date.isAfter(last); date = date.plusDays(1)) {
      if (existingDates.contains(date)) {
        continue;
      }
      DailySummary summary = summaryService.summarizeDay(member, date);
      if (summary.getMealCount() == 0) {
        continue;
      }
      Recommendation recommendation = recommendService.recommend(summary, member);
      reports.add(dailyReportRepository.save(newReport(member, date, summary, recommendation)));
      created = true;
    }

    if (created) {
      reports.sort(Comparator.comparing(DailyReport::getDate));
    }
    return reports;
  }

  private void validateRange(LocalDate from, LocalDate to) {
    if (from.isAfter(to)) {
      throw new InvalidDateRangeException("시작일(from)이 종료일(to)보다 늦어요");
    }
    long days = ChronoUnit.DAYS.between(from, to) + 1;   // from, to 모두 포함
    if (days > MAX_RANGE_DAYS) {
      throw new InvalidDateRangeException(
          "조회 기간은 최대 " + MAX_RANGE_DAYS + "일이에요 (요청: " + days + "일)");
    }
  }

  private DailyReport newReport(Member member, LocalDate date, DailySummary summary,
                                Recommendation recommendation) {
    return new DailyReport(member, date, summary.getMealCount(),
        summary.getTotalKcal(), summary.getTotalCarbohydrate(), summary.getTotalProtein(),
        summary.getTotalFat(),
        recommendation == null ? null : recommendation.getFood(),
        recommendation == null ? null : recommendation.getGrams(),
        LocalDateTime.now(clock));
  }
}
