package com.parkjonghwan.meal_auto;

import static org.assertj.core.api.Assertions.assertThat;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.FoodSource;
import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.MealType;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import com.parkjonghwan.meal_auto.repository.MemberRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

// 주인 기능 이전에 공용으로 저장된 음식을 기록한 회원에게 옮기는지 확인 (메모리 H2, 테스트 후 롤백)
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
  @Autowired MealEntryRepository mealEntryRepository;
  @Autowired MemberRepository memberRepository;

  private static final LocalDate DAY = LocalDate.of(2026, 10, 15);

  @Test
  @DisplayName("예전 공용 음식 중 한 회원만 기록한 음식은 그 회원의 개인 음식이 되고, 기본 음식과 여러 명이 쓴 음식은 공용으로 남는다")
  void assignsLegacyFoodsToTheirOnlyEater() {
    Member a = memberRepository.save(new Member("legacya", "hash"));
    Member b = memberRepository.save(new Member("legacyb", "hash"));

    // 예전 방식: 직접 등록·AI 음식도 주인 없이 저장돼 있었음
    Food onlyA = foodRepository.save(new Food("예전떡볶이", 200, 40, 4, 3));
    Food aiOnlyA = foodRepository.save(new Food("예전AI음식", 300, 30, 10, 10, FoodSource.AI, null));
    Food both = foodRepository.save(new Food("예전김밥", 150, 30, 5, 3));
    Food seed = foodRepository.findFirstByOwnerIsNullAndNameIgnoreCase("쌀밥").orElseThrow();

    mealEntryRepository.save(new MealEntry(a, DAY, MealType.LUNCH, onlyA, 100));
    mealEntryRepository.save(new MealEntry(a, DAY, MealType.LUNCH, aiOnlyA, 100));
    mealEntryRepository.save(new MealEntry(a, DAY, MealType.LUNCH, both, 100));
    mealEntryRepository.save(new MealEntry(b, DAY, MealType.LUNCH, both, 100));
    mealEntryRepository.save(new MealEntry(b, DAY, MealType.LUNCH, seed, 100));

    dataInitializer.run();

    assertThat(onlyA.getOwner()).isEqualTo(a);
    assertThat(onlyA.getSource()).isEqualTo(FoodSource.USER);
    assertThat(aiOnlyA.getOwner()).isEqualTo(a);
    assertThat(aiOnlyA.getSource()).isEqualTo(FoodSource.AI);
    assertThat(both.isShared()).isTrue();
    assertThat(seed.isShared()).isTrue();

    assertThat(foodRepository.findVisibleTo(b)).extracting(Food::getName)
        .contains("예전김밥", "쌀밥")
        .doesNotContain("예전떡볶이", "예전AI음식");
  }
}
