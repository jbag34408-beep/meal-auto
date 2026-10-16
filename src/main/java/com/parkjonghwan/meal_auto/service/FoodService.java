package com.parkjonghwan.meal_auto.service;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.FoodSource;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.FoodRequest;
import com.parkjonghwan.meal_auto.dto.PublicFoodImportRequest;
import com.parkjonghwan.meal_auto.exception.DuplicateFoodException;
import com.parkjonghwan.meal_auto.exception.InvalidNutritionException;
import com.parkjonghwan.meal_auto.exception.PublicFoodNotFoundException;
import com.parkjonghwan.meal_auto.domain.PublicFood;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import com.parkjonghwan.meal_auto.repository.PublicFoodRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FoodService {
  private static final int NAME_MAX = 50;   // FoodRequest의 이름 길이 제한과 같음

  private final FoodRepository foodRepository;
  private final PublicFoodRepository publicFoodRepository;

  public FoodService(FoodRepository foodRepository, PublicFoodRepository publicFoodRepository) {
    this.foodRepository = foodRepository;
    this.publicFoodRepository = publicFoodRepository;
  }

  // 직접 등록한 음식은 그 회원만 쓰는 개인 음식
  @Transactional
  public Food create(Member member, FoodRequest request) {
    String name = request.name().trim();   // 앞뒤 공백 제거 (" 쌀밥" 과 "쌀밥" 을 같게)

    // 내가 볼 수 있는 음식(공용 + 내 음식)과만 겹치지 않으면 됨 (다른 회원의 음식 이름은 상관없음)
    if (foodRepository.existsVisibleByName(member, name)) {
      throw new DuplicateFoodException(name);
    }

    // 필드 하나씩은 @Max로 막았지만, "합계"는 여러 필드를 같이 봐야 해서 여기서 검사
    double total = request.carbohydrate() + request.protein() + request.fat();
    if (total > 100) {
      throw new InvalidNutritionException(
          String.format("100g 안의 탄수화물+단백질+지방 합계(%.1fg)가 100g을 넘을 수 없어요", total));
    }

    return foodRepository.save(new Food(name, request.kcalPer100g(),
        request.carbohydrate(), request.protein(), request.fat(), FoodSource.USER, member));
  }

  // 공공데이터 음식은 모두가 쓰는 공용 음식
  // 화면이 보낸 영양 값은 믿지 않고, 내장된 식약처 DB에서 이름·제조사로 다시 찾은 값만 저장
  // 이미 같은 이름의 공용 음식이 있으면 새로 만들지 않고 그대로 돌려줌
  @Transactional
  public Food importPublic(PublicFoodImportRequest request) {
    String name = request.name().trim();
    String maker = blankToEmpty(request.maker());

    PublicFood found = publicFoodRepository.findFirstByNameAndMaker(name, maker)
        .orElseThrow(() -> new PublicFoodNotFoundException(name));

    String savedName = name.length() > NAME_MAX ? name.substring(0, NAME_MAX) : name;
    return foodRepository.findFirstByOwnerIsNullAndNameIgnoreCase(savedName)
        .orElseGet(() -> foodRepository.save(new Food(savedName, found.getKcal(),
            found.getCarbohydrate(), found.getProtein(), found.getFat(), FoodSource.PUBLIC, null)));
  }

  // 공용 음식 + 내 음식만
  @Transactional(readOnly = true)
  public List<Food> search(Member member, String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return foodRepository.findVisibleTo(member);
    }
    return foodRepository.searchVisibleTo(member, keyword.trim());
  }

  // 내장된 식약처 DB 검색 (최대 20건)
  @Transactional(readOnly = true)
  public List<PublicFood> searchPublic(String keyword) {
    return publicFoodRepository.findTop20ByNameContainingOrderByNameAsc(keyword.trim());
  }

  // 식약처 DB를 아직 안 넣었으면 화면에서 검색 기능을 감춤
  @Transactional(readOnly = true)
  public boolean hasPublicFoods() {
    return publicFoodRepository.existsByIdNotNull();
  }

  private static String blankToEmpty(String value) {
    return Objects.requireNonNullElse(value, "").trim();
  }
}
