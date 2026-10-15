package com.parkjonghwan.meal_auto;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.FoodSource;
import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.MealType;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import com.parkjonghwan.meal_auto.repository.MemberRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
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

  // 공용 기본 음식 이름 (주인 정리 때 이 음식들은 공용으로 남김)
  private static final Set<String> SEED_NAMES = Set.of("쌀밥", "닭가슴살", "바나나", "아몬드", "계란");

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
    // 1) 공용 기본 음식 (처음 한 번만)
    if (foodRepository.count() == 0) {
      foodRepository.save(new Food("쌀밥", 200, 45, 4, 0.5));
      foodRepository.save(new Food("닭가슴살", 110, 0, 23, 1));
      foodRepository.save(new Food("바나나", 90, 23, 1, 0.3));
      foodRepository.save(new Food("아몬드", 579, 22, 21, 50));
      foodRepository.save(new Food("계란", 155, 1.1, 13, 11));
    }

    // 2) 데모 회원과 샘플 기록 (처음 한 번만)
    if (!memberRepository.existsByUsername("demo")) {
      createDemo();
    }

    // 3) 주인 기능 이전에 만들어진 음식의 주인 찾기 (매번 실행해도 결과는 같음)
    assignLegacyFoodOwners();
  }

  private void createDemo() {
    Member demo = memberRepository.save(new Member("demo", passwordEncoder.encode("demo1234")));

    // 회원 기능 이전에 저장된 기록은 demo 회원에게 넘김 (데이터 보존)
    List<MealEntry> orphans = mealEntryRepository.findByMemberIsNull();
    if (!orphans.isEmpty()) {
      orphans.forEach(entry -> entry.assignMember(demo));   // 변경 감지로 자동 저장
      return;
    }

    // 예전 기록이 없으면 샘플 기록 생성
    LocalDate today = LocalDate.now(clock);
    foodRepository.findFirstByOwnerIsNullAndNameIgnoreCase("바나나").ifPresent(f ->
        mealEntryRepository.save(new MealEntry(demo, today, MealType.BREAKFAST, f, 120)));
    foodRepository.findFirstByOwnerIsNullAndNameIgnoreCase("쌀밥").ifPresent(f ->
        mealEntryRepository.save(new MealEntry(demo, today, MealType.LUNCH, f, 150)));
    foodRepository.findFirstByOwnerIsNullAndNameIgnoreCase("닭가슴살").ifPresent(f ->
        mealEntryRepository.save(new MealEntry(demo, today, MealType.DINNER, f, 200)));
  }

  // 예전에는 모든 음식이 공용이라, 회원이 직접 등록하거나 AI로 만든 음식도 주인 없이 저장됨
  // 기본 음식·공공데이터가 아닌 음식 중 기록한 회원이 딱 한 명이면 그 회원의 개인 음식으로 옮김
  // - 기록한 회원이 없거나 여러 명이면 누구 것인지 알 수 없어 공용으로 둠
  //   (그중 AI 음식은 MealRecommendService가 추천 후보에서 뺌)
  // - 직접 등록과 공공데이터 가져오기는 예전 데이터로는 구분이 안 돼 USER로 표시
  private void assignLegacyFoodOwners() {
    int moved = 0;
    for (Food food : foodRepository.findByOwnerIsNull()) {
      if (food.getSource() == FoodSource.PUBLIC
          || (food.getSource() == FoodSource.SEED && SEED_NAMES.contains(food.getName()))) {
        continue;
      }
      List<Member> eaters = mealEntryRepository.findMembersWhoAte(food);
      if (eaters.size() == 1) {
        FoodSource source = food.getSource() == FoodSource.AI ? FoodSource.AI : FoodSource.USER;
        food.assignOwner(eaters.get(0), source);   // 변경 감지로 자동 저장
        moved++;
      }
    }
    if (moved > 0) {
      log.info("주인 없는 예전 음식 {}개를 기록한 회원의 개인 음식으로 옮겼어요", moved);
    }
  }
}
