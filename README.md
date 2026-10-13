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
| GET | `/foods` | 음식 목록 |
| GET | `/meals?date=YYYY-MM-DD` | 그날 식사 기록 (생략 시 오늘) |
| POST | `/meals` | 식사 기록 추가 `{"mealType":"SNACK","foodId":5,"grams":50}` |
| GET | `/summary?date=` | 하루 영양 요약 |
| GET | `/recommendation?date=` | 다음 끼니 추천 |

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
