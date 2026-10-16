package com.parkjonghwan.meal_auto.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.FoodSource;
import com.parkjonghwan.meal_auto.domain.MealType;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.MealBulkRequest;
import com.parkjonghwan.meal_auto.exception.FoodNotFoundException;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import com.parkjonghwan.meal_auto.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// 상차림 한 번에 기록: 실제 트랜잭션 취소를 보려고 테스트 자체에는 @Transactional을 붙이지 않음
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:meal-bulk-test;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "gemini.api-key="
})
class MealBulkTest {

  @Autowired MealController mealController;
  @Autowired FoodRepository foodRepository;
  @Autowired MealEntryRepository mealEntryRepository;
  @Autowired MemberRepository memberRepository;

  private static final LocalDate DAY = LocalDate.of(2026, 10, 15);

  @Test
  @DisplayName("남의 개인 음식이 하나라도 섞이면 404이고, 앞서 저장한 음식도 모두 취소된다")
  void rollsBackWhenAnyFoodIsNotVisible() {
    Member me = memberRepository.save(new Member("bulkme", "hash"));
    Member other = memberRepository.save(new Member("bulkother", "hash"));
    Food rice = foodRepository.findFirstByOwnerIsNullAndNameIgnoreCase("쌀밥").orElseThrow();
    Food othersFood = foodRepository.save(new Food("남의떡볶이", 200, 40, 4, 3, FoodSource.USER, other));

    var request = new MealBulkRequest(DAY, MealType.LUNCH, List.of(
        new MealBulkRequest.Item(rice.getId(), 210),
        new MealBulkRequest.Item(othersFood.getId(), 100)));

    assertThatThrownBy(() -> mealController.addMeals(() -> "bulkme", request))
        .isInstanceOf(FoodNotFoundException.class);
    assertThat(mealEntryRepository.findByMemberAndDate(me, DAY)).isEmpty();
  }
}
