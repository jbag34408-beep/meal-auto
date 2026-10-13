package com.parkjonghwan.meal_auto.repository;

import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.Member;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {
  List<MealEntry> findByMemberAndDate(Member member, LocalDate date);   // 회원별 하루 기록
  List<MealEntry> findByMemberIsNull();                                 // 주인 없는 예전 기록
}