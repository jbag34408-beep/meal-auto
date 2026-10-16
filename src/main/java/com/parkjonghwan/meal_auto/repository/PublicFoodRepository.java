package com.parkjonghwan.meal_auto.repository;

import com.parkjonghwan.meal_auto.domain.PublicFood;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicFoodRepository extends JpaRepository<PublicFood, Long> {
  // shortcut: '%키워드%' 검색이라 인덱스를 못 탐 (32만 건 전체를 훑음), 느려지면 PostgreSQL pg_trgm 인덱스 추가
  List<PublicFood> findTop20ByNameContainingOrderByNameAsc(String keyword);

  Optional<PublicFood> findFirstByNameAndMaker(String name, String maker);

  boolean existsByIdNotNull();   // 데이터가 하나라도 들어 있는지 (count보다 빠름)
}
