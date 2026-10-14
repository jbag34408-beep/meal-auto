package com.parkjonghwan.meal_auto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.parkjonghwan.meal_auto.domain.DailyReport;
import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.DailySummary;
import com.parkjonghwan.meal_auto.dto.Recommendation;
import com.parkjonghwan.meal_auto.repository.DailyReportRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DailyReportServiceTest {

  @Mock
  private DailyReportRepository dailyReportRepository;

  private final LocalDate today = LocalDate.of(2026, 10, 13);
  private final Member member = new Member("tester", "encoded");
  private final Food almond = new Food("아몬드", 579, 22, 21, 50);

  @Test
  void 새_리포트를_저장한다() {
    DailyReportService service = new DailyReportService(dailyReportRepository);
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
    DailyReportService service = new DailyReportService(dailyReportRepository);
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
    DailyReportService service = new DailyReportService(dailyReportRepository);
    when(dailyReportRepository.findByMemberAndDate(member, today)).thenReturn(Optional.empty());
    when(dailyReportRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    DailySummary summary = new DailySummary(today, 5, 2100.0, 300.0, 60.0, 50.0);

    DailyReport saved = service.generateAndSave(member, today, summary, null);

    assertNull(saved.getRecommendedFood());
    assertNull(saved.getRecommendedGrams());
  }
}
