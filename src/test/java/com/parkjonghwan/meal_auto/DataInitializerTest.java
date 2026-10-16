package com.parkjonghwan.meal_auto;

import static org.assertj.core.api.Assertions.assertThat;

import com.parkjonghwan.meal_auto.config.SeedFoodCatalog;
import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.FoodSource;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

// 기본 음식 목록이 공용 음식으로 들어가는지 확인 (메모리 H2, 테스트 후 롤백)
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:data-initializer-test;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "gemini.api-key="
})
@Transactional
class DataInitializerTest {

  @Autowired DataInitializer dataInitializer;
  @Autowired FoodRepository foodRepository;

  @Test
  @DisplayName("기본 음식 목록이 모두 공용 음식으로 들어가고, 다시 실행해도 늘어나지 않는다")
  void seedsCatalogOnce() {
    long before = foodRepository.count();
    assertThat(foodRepository.findByOwnerIsNull()).extracting(Food::getName)
        .containsAll(SeedFoodCatalog.names());

    dataInitializer.run();

    assertThat(foodRepository.count()).isEqualTo(before);
    Food honey = foodRepository.findFirstByOwnerIsNullAndNameIgnoreCase("꿀").orElseThrow();
    assertThat(honey.getServingGrams()).isEqualTo(20.0);
    assertThat(honey.getSource()).isEqualTo(FoodSource.SEED);
  }

  @Test
  @DisplayName("예전에 들어간 기본 음식은 영양 값을 그대로 두고 1인분 양만 채운다")
  void fillsServingForExistingSeedWithoutChangingValues() {
    Food rice = foodRepository.findFirstByOwnerIsNullAndNameIgnoreCase("쌀밥").orElseThrow();
    rice.withServingGrams(null);   // 1인분 기능 이전 상태

    dataInitializer.run();

    assertThat(rice.getServingGrams()).isEqualTo(210.0);
    assertThat(rice.getKcalPer100g()).isEqualTo(200.0);
  }
}
