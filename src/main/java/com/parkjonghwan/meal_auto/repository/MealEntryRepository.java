package com.parkjonghwan.meal_auto.repository;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.Member;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {
  List<MealEntry> findByMemberAndDate(Member member, LocalDate date);   // 회원별 하루 기록
  List<MealEntry> findByMemberIsNull();                                 // 주인 없는 예전 기록

  // 기간 안에 이 회원이 먹은 음식 id (추천에서 최근 먹은 음식을 덜 고르기 위해)
  @Query("select distinct e.food.id from MealEntry e"
      + " where e.member = :member and e.date between :from and :to")
  List<Long> findFoodIdsEatenBetween(@Param("member") Member member,
                                     @Param("from") LocalDate from, @Param("to") LocalDate to);

  // 이 음식을 기록한 적 있는 회원들 (주인 없는 예전 음식의 주인을 찾을 때)
  @Query("select distinct e.member from MealEntry e where e.food = :food and e.member is not null")
  List<Member> findMembersWhoAte(@Param("food") Food food);
}