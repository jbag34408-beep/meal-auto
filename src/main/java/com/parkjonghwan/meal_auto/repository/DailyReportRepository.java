package com.parkjonghwan.meal_auto.repository;

import com.parkjonghwan.meal_auto.domain.DailyReport;
import com.parkjonghwan.meal_auto.domain.Member;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {
  Optional<DailyReport> findByMemberAndDate(Member member, LocalDate date);
  List<DailyReport> findByMemberAndDateBetweenOrderByDateAsc(
      Member member, LocalDate from, LocalDate to);
}
