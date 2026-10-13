package com.parkjonghwan.meal_auto;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.MealType;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// 서버가 켜질 때 한 번 실행되어 테스트용 데이터를 DB에 넣음
@Component
public class DataInitializer implements CommandLineRunner {
  private final FoodRepository foodRepository;
  private final MealEntryRepository mealEntryRepository;

  public DataInitializer(FoodRepository foodRepository,
                         MealEntryRepository mealEntryRepository) {
    this.foodRepository = foodRepository;
    this.mealEntryRepository = mealEntryRepository;
  }

  @Override
  public void run(String... args) {
    if (foodRepository.count() > 0) {
      return;   // 이미 데이터가 있으면 다시 넣지 않음 (중복 방지)
    }

    Food rice = foodRepository.save(new Food("쌀밥", 200, 45, 4, 0.5));
    Food chicken = foodRepository.save(new Food("닭가슴살", 110, 0, 23, 1));
    Food banana = foodRepository.save(new Food("바나나", 90, 23, 1, 0.3));
    foodRepository.save(new Food("아몬드", 579, 22, 21, 50));
    foodRepository.save(new Food("계란", 155, 1.1, 13, 11));

    LocalDate today = LocalDate.now();
    mealEntryRepository.save(new MealEntry(today, MealType.BREAKFAST, banana, 120));
    mealEntryRepository.save(new MealEntry(today, MealType.LUNCH, rice, 150));
    mealEntryRepository.save(new MealEntry(today, MealType.DINNER, chicken, 200));
    mealEntryRepository.save(new MealEntry(today.minusDays(1), MealType.LUNCH, rice, 210));
  }
}
