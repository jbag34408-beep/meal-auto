package com.parkjonghwan.meal_auto.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.parkjonghwan.meal_auto.config.SeedFoodCatalog.SeedFood;
import com.parkjonghwan.meal_auto.domain.FoodCategory;
import java.util.EnumMap;
import java.util.List;
import org.junit.jupiter.api.Test;

class SeedFoodCatalogTest {

  private final List<SeedFood> foods = SeedFoodCatalog.foods();

  @Test
  void 기본_음식이_충분히_있고_이름이_겹치지_않는다() {
    assertThat(foods).hasSizeGreaterThanOrEqualTo(60);
    assertThat(SeedFoodCatalog.names()).hasSize(foods.size());
    assertThat(SeedFoodCatalog.names()).contains("쌀밥", "닭가슴살", "바나나", "아몬드", "계란");
  }

  @Test
  void 영양_값이_직접_등록과_같은_기준을_지킨다() {
    // FoodRequest·FoodService와 같은 기준: 100g당 900kcal 이하, 탄+단+지 합 100g 이하, 이름 50자 이하
    assertThat(foods).allSatisfy(f -> {
      assertThat(f.name()).isNotBlank().hasSizeLessThanOrEqualTo(50);
      assertThat(f.kcalPer100g()).isBetween(0.0, 900.0);
      assertThat(f.carbohydrate() + f.protein() + f.fat()).isLessThanOrEqualTo(100.0);
      assertThat(f.servingGrams()).isPositive();
    });
  }

  @Test
  void 한식_상차림에_필요한_자리가_모두_채워져_있다() {
    var byCategory = new EnumMap<FoodCategory, Integer>(FoodCategory.class);
    foods.stream().filter(f -> f.category() != null)
        .forEach(f -> byCategory.merge(f.category(), 1, Integer::sum));

    assertThat(byCategory.get(FoodCategory.RICE)).isGreaterThanOrEqualTo(3);
    assertThat(byCategory.get(FoodCategory.SOUP)).isGreaterThanOrEqualTo(6);
    assertThat(byCategory.get(FoodCategory.MAIN)).isGreaterThanOrEqualTo(8);
    assertThat(byCategory.get(FoodCategory.SIDE)).isGreaterThanOrEqualTo(8);
    assertThat(byCategory.get(FoodCategory.KIMCHI)).isGreaterThanOrEqualTo(1);
  }
}
