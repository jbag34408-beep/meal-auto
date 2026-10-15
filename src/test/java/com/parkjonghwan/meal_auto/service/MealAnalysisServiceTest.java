package com.parkjonghwan.meal_auto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.FoodSource;
import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.MealType;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.AnalyzedFood;
import com.parkjonghwan.meal_auto.dto.MealTextRequest;
import com.parkjonghwan.meal_auto.exception.AnalyzerUnavailableException;
import com.parkjonghwan.meal_auto.exception.NoFoodRecognizedException;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import com.parkjonghwan.meal_auto.repository.MemberRepository;
import com.parkjonghwan.meal_auto.service.ai.FakeNutritionAnalyzer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

// 실제 DB에 저장·롤백되는지까지 확인하는 통합 테스트
// - DB는 메모리 전용 H2를 따로 씀 → 개발용 파일 DB(./data)나 DB_URL(운영 DB)에 절대 닿지 않음
// - 테스트 클래스에 @Transactional을 일부러 붙이지 않음
//   → 붙이면 서비스의 트랜잭션이 테스트 트랜잭션에 합쳐져서 "실패 시 전부 취소"를 확인할 수 없음
//   대신 테스트가 끝날 때마다 만든 데이터를 직접 지움
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:meal-analysis-test;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "gemini.api-key="
})
class MealAnalysisServiceTest {

  // 실제 분석기 대신 가짜 분석기를 쓰도록 등록 (@Primary라서 Gemini 분석기보다 우선)
  @TestConfiguration
  static class FakeAnalyzerConfig {
    @Bean
    @Primary
    FakeNutritionAnalyzer fakeNutritionAnalyzer() {
      return new FakeNutritionAnalyzer();
    }
  }

  // 테스트가 만든 음식만 골라 지우기 위한 이름 표시
  private static final String TEST_PREFIX = "테스트음식-";
  private static final LocalDate DAY = LocalDate.of(2026, 10, 14);

  @Autowired MealAnalysisService mealAnalysisService;
  @Autowired FakeNutritionAnalyzer fakeAnalyzer;
  @Autowired FoodRepository foodRepository;
  @Autowired MealEntryRepository mealEntryRepository;
  @Autowired MemberRepository memberRepository;

  private Member member;
  private final List<Member> createdMembers = new ArrayList<>();

  @BeforeEach
  void setUp() {
    fakeAnalyzer.reset();
    member = newMember();
  }

  // username은 20자 제한 + unique라서 테스트마다 짧고 겹치지 않는 이름 사용
  private Member newMember() {
    String username = "t" + (System.nanoTime() % 1_000_000_000L);
    Member saved = memberRepository.save(new Member(username, "test-password-hash"));
    createdMembers.add(saved);
    return saved;
  }

  @AfterEach
  void tearDown() {
    List<Long> memberIds = createdMembers.stream().map(Member::getId).toList();
    List<MealEntry> mine = mealEntryRepository.findAll().stream()
        .filter(e -> e.getMember() != null && memberIds.contains(e.getMember().getId()))
        .toList();
    mealEntryRepository.deleteAll(mine);

    // 개인 음식이 회원을 가리키므로 음식을 먼저 지우고 회원을 지움
    List<Food> testFoods = foodRepository.findAll().stream()
        .filter(f -> f.getName() != null && f.getName().startsWith(TEST_PREFIX))
        .toList();
    foodRepository.deleteAll(testFoods);

    memberRepository.deleteAll(createdMembers);
    createdMembers.clear();
  }

  // 100g당 100kcal, 탄 10 / 단 5 / 지 3 인 평범한 음식
  private static AnalyzedFood food(String name, double grams) {
    return new AnalyzedFood(name, grams, 100, 10, 5, 3);
  }

  private MealTextRequest request(LocalDate date) {
    return new MealTextRequest(date, MealType.SNACK, "테스트로 먹은 음식");
  }

  private List<MealEntry> entriesOn(LocalDate date) {
    return mealEntryRepository.findByMemberAndDate(member, date);
  }

  private Optional<Food> findFood(String name) {
    return foodRepository.findAll().stream()
        .filter(f -> f.getName() != null && f.getName().equalsIgnoreCase(name))
        .findFirst();
  }

  private long countFoodsNamed(String name) {
    return foodRepository.findAll().stream()
        .filter(f -> f.getName() != null && f.getName().equalsIgnoreCase(name))
        .count();
  }

  @Test
  @DisplayName("처음 보는 음식이면 AI 출처 Food를 만들고 식사 기록을 저장한다")
  void createsAiFoodForUnknownFood() {
    fakeAnalyzer.willReturn(food(TEST_PREFIX + "김치찌개", 300));

    List<MealEntry> saved = mealAnalysisService.recordFromText(member, request(DAY));

    assertThat(saved).hasSize(1);
    Food created = findFood(TEST_PREFIX + "김치찌개").orElseThrow();
    assertThat(created.getSource()).isEqualTo(FoodSource.AI);
    assertThat(created.isShared()).isFalse();   // AI 음식은 만든 회원의 개인 음식
    assertThat(entriesOn(DAY)).hasSize(1);
    assertThat(entriesOn(DAY).get(0).getGrams()).isEqualTo(300);
  }

  @Test
  @DisplayName("같은 음식을 다시 기록하면 Food를 새로 만들지 않고 재사용한다 (대소문자·앞뒤 공백 무시)")
  void reusesExistingFood() {
    fakeAnalyzer.willReturn(food(TEST_PREFIX + "Kimchi Stew", 300));
    mealAnalysisService.recordFromText(member, request(DAY));

    fakeAnalyzer.willReturn(food("  " + TEST_PREFIX + "kimchi stew  ", 200));
    mealAnalysisService.recordFromText(member, request(DAY));

    assertThat(countFoodsNamed(TEST_PREFIX + "Kimchi Stew")).isEqualTo(1);
    assertThat(entriesOn(DAY)).hasSize(2);
  }

  @Test
  @DisplayName("다른 회원이 AI로 만든 같은 이름의 음식은 재사용하지 않고, 내 목록에도 보이지 않는다")
  void doesNotShareAiFoodsBetweenMembers() {
    Member other = newMember();
    // 다른 회원의 AI가 잘못 추정한 값 (100g당 900kcal)
    fakeAnalyzer.willReturn(new AnalyzedFood(TEST_PREFIX + "떡볶이", 300, 900, 10, 5, 3));
    mealAnalysisService.recordFromText(other, request(DAY));

    fakeAnalyzer.willReturn(food(TEST_PREFIX + "떡볶이", 200));
    List<MealEntry> saved = mealAnalysisService.recordFromText(member, request(DAY));

    assertThat(countFoodsNamed(TEST_PREFIX + "떡볶이")).isEqualTo(2);
    assertThat(saved.get(0).getFood().getKcalPer100g()).isEqualTo(100);   // 내 분석 값으로 저장
    assertThat(foodRepository.findVisibleTo(member))
        .filteredOn(f -> f.getName().equals(TEST_PREFIX + "떡볶이"))
        .hasSize(1)
        .allMatch(f -> f.getKcalPer100g() == 100);
  }

  @Test
  @DisplayName("공용 음식과 이름이 같으면 공용 음식을 그대로 쓴다")
  void reusesSharedFood() {
    Food shared = foodRepository.save(new Food(TEST_PREFIX + "현미밥", 150, 32, 3, 1));
    fakeAnalyzer.willReturn(food(TEST_PREFIX + "현미밥", 210));

    List<MealEntry> saved = mealAnalysisService.recordFromText(member, request(DAY));

    assertThat(saved.get(0).getFood().getId()).isEqualTo(shared.getId());
    assertThat(countFoodsNamed(TEST_PREFIX + "현미밥")).isEqualTo(1);
  }

  @Test
  @DisplayName("AI 분석이 실패하면 아무것도 저장하지 않고 AnalyzerUnavailableException을 그대로 던진다")
  void analyzerFailureSavesNothing() {
    fakeAnalyzer.willFail();

    assertThatThrownBy(() -> mealAnalysisService.recordFromText(member, request(DAY)))
        .isInstanceOf(AnalyzerUnavailableException.class);

    assertThat(entriesOn(DAY)).isEmpty();
  }

  @Test
  @DisplayName("말이 안 되는 값(그램 0 등)은 건너뛰고 정상 음식만 저장한다")
  void skipsInvalidItems() {
    fakeAnalyzer.willReturn(
        food(TEST_PREFIX + "밥", 210),
        new AnalyzedFood(TEST_PREFIX + "이상한값", 0, 100, 1, 1, 1));

    List<MealEntry> saved = mealAnalysisService.recordFromText(member, request(DAY));

    assertThat(saved).hasSize(1);
    assertThat(findFood(TEST_PREFIX + "이상한값")).isEmpty();
  }

  @Test
  @DisplayName("저장할 수 있는 음식이 하나도 없으면 NoFoodRecognizedException을 던진다")
  void throwsWhenNoValidFood() {
    fakeAnalyzer.willReturn(new AnalyzedFood(TEST_PREFIX + "이상한값", -5, 100, 1, 1, 1));

    assertThatThrownBy(() -> mealAnalysisService.recordFromText(member, request(DAY)))
        .isInstanceOf(NoFoodRecognizedException.class);

    assertThat(entriesOn(DAY)).isEmpty();
  }

  @Test
  @DisplayName("두 번째 음식 저장 중 실패하면 첫 번째 음식 기록도 함께 취소된다")
  void rollsBackEverythingWhenSaveFailsMidway() {
    // 이름이 DB 컬럼 길이(255자)를 넘어서 두 번째 저장에서 DB 에러가 남
    String tooLongName = TEST_PREFIX + "가".repeat(300);
    fakeAnalyzer.willReturn(
        food(TEST_PREFIX + "정상음식", 200),
        food(tooLongName, 100));

    assertThatThrownBy(() -> mealAnalysisService.recordFromText(member, request(DAY)))
        .isInstanceOf(RuntimeException.class);

    assertThat(entriesOn(DAY)).isEmpty();
    assertThat(findFood(TEST_PREFIX + "정상음식")).isEmpty();
  }

  @Test
  @DisplayName("날짜를 보내지 않으면 오늘 날짜로 저장한다")
  void usesTodayWhenDateOmitted() {
    fakeAnalyzer.willReturn(food(TEST_PREFIX + "사과", 150));

    mealAnalysisService.recordFromText(member, request(null));

    assertThat(entriesOn(LocalDate.now())).hasSize(1);
  }
}
