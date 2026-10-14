package com.parkjonghwan.meal_auto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.parkjonghwan.meal_auto.domain.DailyReport;
import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.dto.Recommendation;
import com.parkjonghwan.meal_auto.exception.InvalidDateRangeException;
import com.parkjonghwan.meal_auto.repository.DailyReportRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DailyReportServiceTest {

  @Mock
  private DailyReportRepository dailyReportRepository;
  @Mock
  private MealSummaryService summaryService;
  @Mock
  private MealRecommendService recommendService;

  private final LocalDate today = LocalDate.of(2026, 10, 13);
  private final Member member = new Member("tester", "encoded");
  private final Food almond = new Food("아몬드", 579, 22, 21, 50);

  private DailyReportService service;

  @BeforeEach
  void setUp() {
    // "오늘"을 2026-10-13 12:00 (한국 시간)으로 고정
    ZoneId seoul = ZoneId.of("Asia/Seoul");
    Clock fixedClock = Clock.fixed(
        ZonedDateTime.of(today.atTime(12, 0), seoul).toInstant(), seoul);
    service = new DailyReportService(dailyReportRepository, summaryService, recommendService,
        fixedClock);
  }

  private DailyReport savedReport(LocalDate date) {
    return new DailyReport(member, date, 2, 400.0, 50.0, 20.0, 10.0,
        null, null, date.atTime(21, 0));
  }

  private DailySummary summaryOf(LocalDate date, int mealCount) {
    return new DailySummary(date, mealCount, 500.0 * mealCount, 60.0, 20.0, 10.0);
  }

  @Test
  void 새_리포트를_저장한다() {
    when(dailyReportRepository.findByMemberAndDate(member, today)).thenReturn(Optional.empty());
    when(dailyReportRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    DailySummary summary = new DailySummary(today, 3, 628.0, 95.1, 53.2, 3.1);
    Recommendation recommendation = new Recommendation(almond, 94.0, "지방이 부족해요",
        "지방", 94.0, 544.3, 1372.0, false, List.of());

    DailyReport saved = service.generateAndSave(member, today, summary, recommendation);

    assertEquals(today, saved.getDate());
    assertEquals(3, saved.getMealCount());
    assertEquals(628.0, saved.getTotalKcal(), 0.001);
    assertEquals("아몬드", saved.getRecommendedFood().getName());
    assertEquals(94.0, saved.getRecommendedGrams(), 0.001);
  }

  @Test
  void 같은_날_다시_생성하면_기존_것을_갱신한다() {
    DailyReport existing = new DailyReport(member, today, 2, 400.0, 50.0, 20.0, 10.0,
        null, null, LocalDateTime.of(2026, 10, 13, 21, 0));
    when(dailyReportRepository.findByMemberAndDate(member, today)).thenReturn(Optional.of(existing));
    when(dailyReportRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    DailySummary summary = new DailySummary(today, 3, 628.0, 95.1, 53.2, 3.1);
    Recommendation recommendation = new Recommendation(almond, 94.0, "지방이 부족해요",
        "지방", 94.0, 544.3, 1372.0, false, List.of());

    DailyReport result = service.generateAndSave(member, today, summary, recommendation);

    ArgumentCaptor<DailyReport> captor = ArgumentCaptor.forClass(DailyReport.class);
    verify(dailyReportRepository).save(captor.capture());
    assertSame(existing, captor.getValue());   // 새로 만들지 않고 기존 엔티티를 그대로 갱신
    assertEquals(3, result.getMealCount());
    assertEquals(628.0, result.getTotalKcal(), 0.001);
    assertEquals(LocalDateTime.of(2026, 10, 13, 21, 0), result.getCreatedAt());   // 최초 생성 시각 유지
  }

  @Test
  void 목표를_다_채운_날은_추천이_null로_저장된다() {
    when(dailyReportRepository.findByMemberAndDate(member, today)).thenReturn(Optional.empty());
    when(dailyReportRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    DailySummary summary = new DailySummary(today, 5, 2100.0, 300.0, 60.0, 50.0);

    DailyReport saved = service.generateAndSave(member, today, summary, null);

    assertNull(saved.getRecommendedFood());
    assertNull(saved.getRecommendedGrams());
  }

  @Test
  void 리포트가_빠진_날짜는_만들어_저장하고_날짜순으로_돌려준다() {
    LocalDate from = today.minusDays(3);   // 10/10
    LocalDate to = today.minusDays(1);     // 10/12
    DailyReport existing = savedReport(today.minusDays(2));   // 10/11만 저장돼 있음
    when(dailyReportRepository.findByMemberAndDateBetweenOrderByDateAsc(member, from, to))
        .thenReturn(List.of(existing));
    when(summaryService.summarizeDay(eq(member), any()))
        .thenAnswer(invocation -> summaryOf(invocation.getArgument(1), 2));
    Recommendation recommendation = new Recommendation(almond, 30.0, "지방이 부족해요",
        "지방", 30.0, 170.0, 1000.0, false, List.of());
    when(recommendService.recommend(any(), eq(member))).thenReturn(recommendation);
    when(dailyReportRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    List<DailyReport> reports = service.getReports(member, from, to);

    assertEquals(List.of(from, today.minusDays(2), to),
        reports.stream().map(DailyReport::getDate).toList());
    verify(dailyReportRepository, times(2)).save(any());
    DailyReport created = reports.get(0);
    assertEquals(2, created.getMealCount());
    assertEquals(1000.0, created.getTotalKcal(), 0.001);
    assertEquals("아몬드", created.getRecommendedFood().getName());
    assertEquals(LocalDateTime.of(2026, 10, 13, 12, 0), created.getCreatedAt());
  }

  @Test
  void 이미_리포트가_있는_날은_다시_계산하거나_저장하지_않는다() {
    LocalDate from = today.minusDays(2);
    LocalDate to = today.minusDays(1);
    List<DailyReport> existing = List.of(savedReport(from), savedReport(to));
    when(dailyReportRepository.findByMemberAndDateBetweenOrderByDateAsc(member, from, to))
        .thenReturn(existing);

    List<DailyReport> reports = service.getReports(member, from, to);

    assertEquals(existing, reports);
    assertSame(existing.get(0), reports.get(0));
    verifyNoInteractions(summaryService, recommendService);
    verify(dailyReportRepository, never()).save(any());
  }

  @Test
  void 오늘과_미래_날짜는_리포트를_만들지_않는다() {
    LocalDate from = today.minusDays(1);
    LocalDate to = today.plusDays(1);
    when(dailyReportRepository.findByMemberAndDateBetweenOrderByDateAsc(member, from, to))
        .thenReturn(List.of());
    when(summaryService.summarizeDay(member, from)).thenReturn(summaryOf(from, 1));
    when(dailyReportRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    List<DailyReport> reports = service.getReports(member, from, to);

    assertEquals(List.of(from), reports.stream().map(DailyReport::getDate).toList());
    verify(summaryService, never()).summarizeDay(member, today);
    verify(summaryService, never()).summarizeDay(member, today.plusDays(1));
  }

  @Test
  void 식사_기록이_0건인_날은_저장하지_않고_응답에서도_뺀다() {
    LocalDate day = today.minusDays(1);
    when(dailyReportRepository.findByMemberAndDateBetweenOrderByDateAsc(member, day, day))
        .thenReturn(List.of());
    when(summaryService.summarizeDay(member, day)).thenReturn(summaryOf(day, 0));

    List<DailyReport> reports = service.getReports(member, day, day);

    assertTrue(reports.isEmpty());
    verifyNoInteractions(recommendService);
    verify(dailyReportRepository, never()).save(any());
  }

  @Test
  void 시작일이_종료일보다_늦으면_예외() {
    assertThrows(InvalidDateRangeException.class,
        () -> service.getReports(member, today.minusDays(1), today.minusDays(2)));
    verifyNoInteractions(dailyReportRepository, summaryService, recommendService);
  }

  @Test
  void 기간이_31일을_넘으면_예외() {
    LocalDate from = LocalDate.of(2026, 9, 1);

    // 9/1 ~ 10/2 = 32일 → 거절
    assertThrows(InvalidDateRangeException.class,
        () -> service.getReports(member, from, from.plusDays(31)));
    verifyNoInteractions(dailyReportRepository, summaryService, recommendService);
  }

  @Test
  void 기간이_딱_31일이면_허용한다() {
    LocalDate from = LocalDate.of(2026, 9, 1);
    LocalDate to = from.plusDays(30);   // 9/1 ~ 10/1 = 31일
    List<DailyReport> existing = new ArrayList<>();
    for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
      existing.add(savedReport(d));
    }
    when(dailyReportRepository.findByMemberAndDateBetweenOrderByDateAsc(member, from, to))
        .thenReturn(existing);

    assertEquals(31, service.getReports(member, from, to).size());
  }
}
