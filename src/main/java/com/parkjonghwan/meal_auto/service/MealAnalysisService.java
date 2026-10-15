package com.parkjonghwan.meal_auto.service;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.domain.MealEntry;
import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.AnalyzedFood;
import com.parkjonghwan.meal_auto.dto.MealTextRequest;
import com.parkjonghwan.meal_auto.exception.NoFoodRecognizedException;
import com.parkjonghwan.meal_auto.repository.FoodRepository;
import com.parkjonghwan.meal_auto.repository.MealEntryRepository;
import com.parkjonghwan.meal_auto.service.ai.NutritionAnalyzer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

// 텍스트로 받은 식사 설명을 AI로 분석해서 식사 기록으로 저장
@Service
public class MealAnalysisService {
  private final NutritionAnalyzer nutritionAnalyzer;
  private final FoodRepository foodRepository;
  private final MealEntryRepository mealEntryRepository;
  private final TransactionTemplate transactionTemplate;

  public MealAnalysisService(NutritionAnalyzer nutritionAnalyzer,
                             FoodRepository foodRepository,
                             MealEntryRepository mealEntryRepository,
                             PlatformTransactionManager transactionManager) {
    this.nutritionAnalyzer = nutritionAnalyzer;
    this.foodRepository = foodRepository;
    this.mealEntryRepository = mealEntryRepository;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
  }

  public List<MealEntry> recordFromText(Member member, MealTextRequest request) {
    // 1) AI 분석은 트랜잭션 "밖"에서 함
    //    AI 응답은 몇 초씩 걸릴 수 있는데, 그동안 DB 연결을 붙잡고 있으면
    //    다른 사용자 요청이 DB 연결을 못 얻고 줄을 서게 됨 (연결 풀이 5개뿐)
    //    AnalyzerUnavailableException은 여기서 삼키지 않고 그대로 위로 던짐 (컨트롤러가 503 처리)
    List<AnalyzedFood> analyzed = nutritionAnalyzer.analyze(request.text());

    // 2) 말이 안 되는 값은 걸러냄 (그램 0, 음수 칼로리 등)
    List<AnalyzedFood> validFoods = analyzed == null ? List.of()
        : analyzed.stream().filter(AnalyzedFood::isValid).toList();

    if (validFoods.isEmpty()) {
      throw new NoFoodRecognizedException(request.text());
    }

    LocalDate date = request.date() != null ? request.date() : LocalDate.now();

    // 3) 저장은 트랜잭션 "안"에서 한 번에
    //    중간에 하나라도 실패하면 앞에서 저장한 것까지 전부 취소됨
    return transactionTemplate.execute(status -> {
      List<MealEntry> saved = new ArrayList<>();
      for (AnalyzedFood item : validFoods) {
        Food food = findOrCreateFood(member, item);
        saved.add(mealEntryRepository.save(
            new MealEntry(member, date, request.mealType(), food, item.grams())));
      }
      return saved;
    });
  }

  // 내가 볼 수 있는 음식(내 음식 → 공용 순) 중 같은 이름(대소문자·앞뒤 공백 무시)이 있으면 재사용,
  // 없으면 AI 출처의 내 개인 음식으로 새로 만듦 (다른 회원의 음식은 쓰지도, 보이지도 않음)
  private Food findOrCreateFood(Member member, AnalyzedFood item) {
    return foodRepository.findVisibleByNameIgnoreCase(member, item.normalizedName()).stream()
        .findFirst()
        .orElseGet(() -> foodRepository.save(item.toFood(member)));
  }
}
