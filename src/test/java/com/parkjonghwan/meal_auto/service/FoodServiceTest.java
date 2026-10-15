package com.parkjonghwan.meal_auto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.FoodSource;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.FoodRequest;
import com.parkjonghwan.meal_auto.dto.FoodSearchResult;
import com.parkjonghwan.meal_auto.dto.PublicFoodImportRequest;
import com.parkjonghwan.meal_auto.exception.DuplicateFoodException;
import com.parkjonghwan.meal_auto.exception.InvalidNutritionException;
import com.parkjonghwan.meal_auto.exception.PublicFoodNotFoundException;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FoodServiceTest {

  @Mock
  private FoodRepository foodRepository;

  @Mock
  private PublicFoodClient publicFoodClient;

  @InjectMocks
  private FoodService foodService;

  private final Member member = new Member("tester", "encoded");

  @Test
  void 정상_등록되면_이름의_앞뒤_공백을_제거해서_저장() {
    // given: save가 받은 객체를 그대로 돌려주도록 설정
    when(foodRepository.existsVisibleByName(member, "고구마")).thenReturn(false);
    when(foodRepository.save(any(Food.class))).thenAnswer(inv -> inv.getArgument(0));

    // when
    Food saved = foodService.create(member, new FoodRequest("  고구마 ", 130, 31, 1.5, 0.2));

    // then
    assertEquals("고구마", saved.getName());
    assertEquals(130.0, saved.getKcalPer100g(), 0.001);
    // 직접 등록한 음식은 등록한 회원의 개인 음식
    assertSame(member, saved.getOwner());
    assertFalse(saved.isShared());
    assertEquals(FoodSource.USER, saved.getSource());
  }

  @Test
  void 같은_이름이_있으면_409_예외_그리고_저장하지_않음() {
    when(foodRepository.existsVisibleByName(member, "쌀밥")).thenReturn(true);

    assertThrows(DuplicateFoodException.class,
        () -> foodService.create(member, new FoodRequest("쌀밥", 200, 45, 4, 0.5)));

    verify(foodRepository, never()).save(any());   // save가 한 번도 불리지 않았는지 확인
  }

  @Test
  void 영양소_합이_100g을_넘으면_예외() {
    when(foodRepository.existsVisibleByName(member, "이상한음식")).thenReturn(false);

    // 60 + 30 + 20 = 110g → 100g 안에 들어갈 수 없음
    assertThrows(InvalidNutritionException.class,
        () -> foodService.create(member, new FoodRequest("이상한음식", 500, 60, 30, 20)));

    verify(foodRepository, never()).save(any());
  }

  @Test
  void 공공데이터_가져오기는_서버가_다시_조회한_값으로_공용_음식을_만든다() {
    when(publicFoodClient.search("사과")).thenReturn(List.of(
        new FoodSearchResult("사과즙", "", "100g", 45, 11, 0.1, 0),
        new FoodSearchResult("사과", "", "100g", 52, 14, 0.3, 0.2)));
    when(foodRepository.findFirstByOwnerIsNullAndNameIgnoreCase("사과")).thenReturn(Optional.empty());
    when(foodRepository.save(any(Food.class))).thenAnswer(inv -> inv.getArgument(0));

    Food saved = foodService.importPublic(new PublicFoodImportRequest("사과", null));

    assertEquals(52.0, saved.getKcalPer100g(), 0.001);
    assertTrue(saved.isShared());
    assertEquals(FoodSource.PUBLIC, saved.getSource());
  }

  @Test
  void 같은_이름의_공용_음식이_있으면_새로_만들지_않는다() {
    Food existing = new Food("사과", 52, 14, 0.3, 0.2);
    when(publicFoodClient.search("사과")).thenReturn(List.of(
        new FoodSearchResult("사과", null, "100g", 52, 14, 0.3, 0.2)));
    when(foodRepository.findFirstByOwnerIsNullAndNameIgnoreCase("사과")).thenReturn(Optional.of(existing));

    assertSame(existing, foodService.importPublic(new PublicFoodImportRequest("사과", "")));
    verify(foodRepository, never()).save(any());
  }

  @Test
  void 공공데이터에_없는_음식은_가져올_수_없다() {
    when(publicFoodClient.search("가짜음식")).thenReturn(List.of());

    assertThrows(PublicFoodNotFoundException.class,
        () -> foodService.importPublic(new PublicFoodImportRequest("가짜음식", "")));
    verify(foodRepository, never()).save(any());
  }
}
