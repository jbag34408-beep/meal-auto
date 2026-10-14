package com.parkjonghwan.meal_auto.repository;

import com.parkjonghwan.meal_auto.domain.Food;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodRepository extends JpaRepository<Food, Long> {
  boolean existsByName(String name);
  Optional<Food> findByName(String name);
  List<Food> findByNameContainingOrderByNameAsc(String keyword);

  // AI 분석 결과를 기존 음식과 맞출 때 사용
  // 대소문자를 무시하고, 같은 이름이 여러 개 있어도 첫 번째 하나만 가져옴
  // (findByName은 같은 이름이 2개 이상이면 예외가 나서 따로 둠)
  Optional<Food> findFirstByNameIgnoreCase(String name);
}
