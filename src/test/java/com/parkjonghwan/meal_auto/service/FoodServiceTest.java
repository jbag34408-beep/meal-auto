package com.parkjonghwan.meal_auto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.dto.FoodRequest;
import com.parkjonghwan.meal_auto.exception.DuplicateFoodException;
import com.parkjonghwan.meal_auto.exception.InvalidNutritionException;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FoodServiceTest {

  @Mock
  private FoodRepository foodRepository;

  @InjectMocks
  private FoodService foodService;

  @Test
  void 정상_등록되면_이름의_앞뒤_공백을_제거해서_저장() {
    // given: save가 받은 객체를 그대로 돌려주도록 설정
    when(foodRepository.existsByName("고구마")).thenReturn(false);
    when(foodRepository.save(any(Food.class))).thenAnswer(inv -> inv.getArgument(0));

    // when
    Food saved = foodService.create(new FoodRequest("  고구마 ", 130, 31, 1.5, 0.2));

    // then
    assertEquals("고구마", saved.getName());
    assertEquals(130.0, saved.getKcalPer100g(), 0.001);
  }

  @Test
  void 같은_이름이_있으면_409_예외_그리고_저장하지_않음() {
    when(foodRepository.existsByName("쌀밥")).thenReturn(true);

    assertThrows(DuplicateFoodException.class,
        () -> foodService.create(new FoodRequest("쌀밥", 200, 45, 4, 0.5)));

    verify(foodRepository, never()).save(any());   // save가 한 번도 불리지 않았는지 확인
  }

  @Test
  void 영양소_합이_100g을_넘으면_예외() {
    when(foodRepository.existsByName("이상한음식")).thenReturn(false);

    // 60 + 30 + 20 = 110g → 100g 안에 들어갈 수 없음
    assertThrows(InvalidNutritionException.class,
        () -> foodService.create(new FoodRequest("이상한음식", 500, 60, 30, 20)));

    verify(foodRepository, never()).save(any());
  }
}