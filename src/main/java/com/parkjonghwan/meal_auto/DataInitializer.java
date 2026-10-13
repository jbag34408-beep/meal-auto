package com.parkjonghwan.meal_auto;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.MealType;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import com.parkjonghwan.meal_auto.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
  private final FoodRepository foodRepository;
  private final MealEntryRepository mealEntryRepository;
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  public DataInitializer(FoodRepository foodRepository,
                         MealEntryRepository mealEntryRepository,
                         MemberRepository memberRepository,
                         PasswordEncoder passwordEncoder) {
    this.foodRepository = foodRepository;
    this.mealEntryRepository = mealEntryRepository;
    this.memberRepository = memberRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(String... args) {
    // 1) 기본 음식 (처음 한 번만)
    if (foodRepository.count() == 0) {
      foodRepository.save(new Food("쌀밥", 200, 45, 4, 0.5));
      foodRepository.save(new Food("닭가슴살", 110, 0, 23, 1));
      foodRepository.save(new Food("바나나", 90, 23, 1, 0.3));
      foodRepository.save(new Food("아몬드", 579, 22, 21, 50));
      foodRepository.save(new Food("계란", 155, 1.1, 13, 11));
    }

    // 2) 데모 회원 (처음 한 번만)
    if (memberRepository.existsByUsername("demo")) {
      return;
    }
    Member demo = memberRepository.save(new Member("demo", passwordEncoder.encode("demo1234")));

    // 3) 회원 기능 이전에 저장된 기록은 demo 회원에게 넘김 (데이터 보존)
    List<MealEntry> orphans = mealEntryRepository.findByMemberIsNull();
    if (!orphans.isEmpty()) {
      orphans.forEach(entry -> entry.assignMember(demo));   // 변경 감지로 자동 저장
      return;
    }

    // 4) 예전 기록이 없으면 샘플 기록 생성
    LocalDate today = LocalDate.now();
    foodRepository.findByName("바나나").ifPresent(f ->
        mealEntryRepository.save(new MealEntry(demo, today, MealType.BREAKFAST, f, 120)));
    foodRepository.findByName("쌀밥").ifPresent(f ->
        mealEntryRepository.save(new MealEntry(demo, today, MealType.LUNCH, f, 150)));
    foodRepository.findByName("닭가슴살").ifPresent(f ->
        mealEntryRepository.save(new MealEntry(demo, today, MealType.DINNER, f, 200)));
  }
}