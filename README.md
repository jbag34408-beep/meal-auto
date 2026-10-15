# meal-auto

Spring Boot로 만든 식단 자동화 서비스예요. 영양 계산, 하루 요약, 다음 끼니 추천, 매일 자동 리포트를 지원해요.

## 기술
Java 17 · Spring Boot 4.1.1 · Spring Data JPA · H2 (파일 DB)

## 실행
1. IntelliJ에서 `File → Open`으로 이 폴더를 열어요 (Gradle 프로젝트로 인식).
2. `MealAutoApplication`을 실행해요.

## API
| 메서드 | 주소 | 설명 |
|---|---|---|
| GET | `/foods` | 음식 목록 (공용 음식 + 내 음식) |
| POST | `/foods` | 내 음식 직접 등록 (나만 보임) |
| GET | `/foods/public?keyword=` | 식약처 DB 검색 (저장 안 함) |
| POST | `/foods/public` | 식약처 음식을 공용 음식으로 가져오기 `{"name":"사과","maker":""}` (영양 값은 서버가 다시 조회) |
| GET | `/meals?date=YYYY-MM-DD` | 그날 식사 기록 (생략 시 오늘) |
| POST | `/meals` | 식사 기록 추가 `{"mealType":"SNACK","foodId":5,"grams":50}` |
| GET | `/summary?date=` | 하루 영양 요약 |
| GET | `/recommendation?date=` | 다음 끼니 추천 |

## 음식 데이터 (공용 · 개인)
- 공용: 기본 음식(SEED), 식약처 공공데이터(PUBLIC) → 모든 회원이 목록·기록·추천에 씀
- 개인: 직접 등록(USER), AI 추정(AI) → 만든 회원만 씀 (`food.member_id`), 다른 회원의 목록·추천에 섞이지 않음

## 자동 실행
`DailyMealScheduler`가 매일 21:00(Asia/Seoul)에 요약과 추천을 로그로 남겨요.

## 패키지 구조
```
com.parkjonghwan.meal_auto
├── MealAutoApplication, DataInitializer
├── controller   MealController
├── domain       Food, MealEntry, MealType
├── dto          DailySummary, MealEntryRequest, Recommendation
├── repository   FoodRepository, MealEntryRepository
├── scheduler    DailyMealScheduler
└── service      MealRecommendService, MealSummaryService, NutritionCalculator
```
