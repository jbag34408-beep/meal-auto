package com.parkjonghwan.meal_auto.repository;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 회원이 "볼 수 있는" 음식 = 공용 음식(owner 없음) + 내 개인 음식
// 다른 회원의 개인 음식은 목록·기록·추천 어디에도 나오지 않음
public interface FoodRepository extends JpaRepository<Food, Long> {

  @Query("select f from Food f where f.owner is null or f.owner = :member order by f.name")
  List<Food> findVisibleTo(@Param("member") Member member);

  @Query("select f from Food f where (f.owner is null or f.owner = :member)"
      + " and f.name like concat('%', :keyword, '%') order by f.name")
  List<Food> searchVisibleTo(@Param("member") Member member, @Param("keyword") String keyword);

  @Query("select f from Food f where f.id = :id and (f.owner is null or f.owner = :member)")
  Optional<Food> findVisibleById(@Param("member") Member member, @Param("id") Long id);

  @Query("select count(f) > 0 from Food f where (f.owner is null or f.owner = :member) and f.name = :name")
  boolean existsVisibleByName(@Param("member") Member member, @Param("name") String name);

  // AI 분석 결과를 기존 음식과 맞출 때 사용 (대소문자 무시, 내 음식을 공용보다 먼저)
  @Query("select f from Food f where (f.owner is null or f.owner = :member)"
      + " and lower(f.name) = lower(:name)"
      + " order by case when f.owner is null then 1 else 0 end, f.id")
  List<Food> findVisibleByNameIgnoreCase(@Param("member") Member member, @Param("name") String name);

  // 공용 음식만 (기본 음식 찾기, 공공데이터 중복 확인)
  Optional<Food> findFirstByOwnerIsNullAndNameIgnoreCase(String name);
  List<Food> findByOwnerIsNull();
}
