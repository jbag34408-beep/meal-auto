package com.parkjonghwan.meal_auto.repository;

import com.parkjonghwan.meal_auto.domain.Food;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodRepository extends JpaRepository<Food, Long> {
  boolean existsByName(String name);
  Optional<Food> findByName(String name);
  List<Food> findByNameContainingOrderByNameAsc(String keyword);
}