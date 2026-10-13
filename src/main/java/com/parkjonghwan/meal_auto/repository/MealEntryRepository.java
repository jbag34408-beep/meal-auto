package com.parkjonghwan.meal_auto.repository;

import com.parkjonghwan.meal_auto.domain.MealEntry;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {
  // 메서드 이름 규칙으로 "날짜로 찾기" 쿼리 자동 생성
  List<MealEntry> findByDate(LocalDate date);
}
