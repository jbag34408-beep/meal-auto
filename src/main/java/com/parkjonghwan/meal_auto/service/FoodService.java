package com.parkjonghwan.meal_auto.service;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.dto.FoodRequest;
import com.parkjonghwan.meal_auto.exception.DuplicateFoodException;
import com.parkjonghwan.meal_auto.exception.InvalidNutritionException;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FoodService {
  private final FoodRepository foodRepository;

  public FoodService(FoodRepository foodRepository) {
    this.foodRepository = foodRepository;
  }

  @Transactional
  public Food create(FoodRequest request) {
    String name = request.name().trim();   // 앞뒤 공백 제거 (" 쌀밥" 과 "쌀밥" 을 같게)

    if (foodRepository.existsByName(name)) {
      throw new DuplicateFoodException(name);
    }

    // 필드 하나씩은 @Max로 막았지만, "합계"는 여러 필드를 같이 봐야 해서 여기서 검사
    double total = request.carbohydrate() + request.protein() + request.fat();
    if (total > 100) {
      throw new InvalidNutritionException(
          String.format("100g 안의 탄수화물+단백질+지방 합계(%.1fg)가 100g을 넘을 수 없어요", total));
    }

    return foodRepository.save(new Food(name, request.kcalPer100g(),
        request.carbohydrate(), request.protein(), request.fat()));
  }

  @Transactional(readOnly = true)
  public List<Food> search(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return foodRepository.findAll();
    }
    return foodRepository.findByNameContainingOrderByNameAsc(keyword.trim());
  }
}