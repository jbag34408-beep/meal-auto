package com.parkjonghwan.meal_auto;

import com.parkjonghwan.meal_auto.config.SeedFoodCatalog;
import com.parkjonghwan.meal_auto.config.SeedFoodCatalog.SeedFood;
import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.MealType;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import com.parkjonghwan.meal_auto.repository.MemberRepository;
import java.time.Clock;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)   // FoodSourceColumnFix 다음에 실행
public class DataInitializer implements CommandLineRunner {
  private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

  private final FoodRepository foodRepository;
  private final MealEntryRepository mealEntryRepository;
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;   // 한국 시간 (config/TimeConfig)

  public DataInitializer(FoodRepository foodRepository,
                         MealEntryRepository mealEntryRepository,
                         MemberRepository memberRepository,
                         PasswordEncoder passwordEncoder,
                         Clock clock) {
    this.foodRepository = foodRepository;
    this.mealEntryRepository = mealEntryRepository;
    this.memberRepository = memberRepository;
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void run(String... args) {
    // 1) 공용 기본 음식: 목록(seed/foods.csv) 중 DB에 없는 것만 추가 (배포된 DB에도 새 음식이 들어감)
    addMissingSeedFoods();

    // 2) 데모 회원과 샘플 기록 (처음 한 번만)
    if (!memberRepository.existsByUsername("demo")) {
      createDemo();
    }
  }

  // 이미 있는 음식의 영양 값은 그대로 두고, 1인분 양·상차림 분류가 비어 있으면 채움
  private void addMissingSeedFoods() {
    int added = 0;
    for (SeedFood seed : SeedFoodCatalog.foods()) {
      var existing = foodRepository.findFirstByOwnerIsNullAndNameIgnoreCase(seed.name());
      if (existing.isPresent()) {
        if (existing.get().getServingGrams() == null) {
          existing.get().withServingGrams(seed.servingGrams());   // 변경 감지로 자동 저장
        }
        if (existing.get().getCategory() == null && seed.category() != null) {
          existing.get().withCategory(seed.category());
        }
        continue;
      }
      foodRepository.save(new Food(seed.name(), seed.kcalPer100g(), seed.carbohydrate(),
          seed.protein(), seed.fat()).withServingGrams(seed.servingGrams()).withCategory(seed.category()));
      added++;
    }
    if (added > 0) {
      log.info("공용 기본 음식 {}개를 추가했어요 (전체 목록 {}개)", added, SeedFoodCatalog.foods().size());
    }
  }

  private void createDemo() {
    Member demo = memberRepository.save(new Member("demo", passwordEncoder.encode("demo1234")));

    // 샘플 기록
    LocalDate today = LocalDate.now(clock);
    foodRepository.findFirstByOwnerIsNullAndNameIgnoreCase("바나나").ifPresent(f ->
        mealEntryRepository.save(new MealEntry(demo, today, MealType.BREAKFAST, f, 120)));
    foodRepository.findFirstByOwnerIsNullAndNameIgnoreCase("쌀밥").ifPresent(f ->
        mealEntryRepository.save(new MealEntry(demo, today, MealType.LUNCH, f, 150)));
    foodRepository.findFirstByOwnerIsNullAndNameIgnoreCase("닭가슴살").ifPresent(f ->
        mealEntryRepository.save(new MealEntry(demo, today, MealType.DINNER, f, 200)));
  }
}
