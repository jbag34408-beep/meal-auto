package com.parkjonghwan.meal_auto.repository;

import com.parkjonghwan.meal_auto.domain.Food;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodRepository extends JpaRepository<Food, Long> {
  boolean existsByName(String name);                                // 같은 이름이 있는지
  List<Food> findByNameContainingOrderByNameAsc(String keyword);    // 이름에 키워드가 포함된 음식 (SQL의 LIKE '%키워드%')
}