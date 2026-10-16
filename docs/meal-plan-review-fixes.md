# 식단 추천(상차림) 리뷰 후속 수정

커밋 `d51a787`(식단 추천을 끼니별 한식 상차림으로 바꿈) 리뷰에서 나온 두 가지를 고쳤다.

## 1. 상차림 한 번에 기록(`POST /meals/bulk`) 테스트 추가

- 파일: `src/test/java/com/parkjonghwan/meal_auto/controller/MealBulkTest.java`
- 문제: 음식 여러 개를 한 트랜잭션으로 저장하는 API인데 테스트가 없었다.
  `@Transactional`이 빠지거나 `findVisibleById`가 `findById`로 바뀌어도 알 수 없었다.
- 테스트 내용: 공용 음식(쌀밥) + 다른 회원의 개인 음식을 한 상차림으로 기록
  → `FoodNotFoundException`(404)이 나고, 먼저 저장된 쌀밥 기록까지 취소되어 그날 기록이 0개여야 한다.
- 테스트 클래스에는 `@Transactional`을 붙이지 않았다 (붙이면 실제 취소 여부를 확인할 수 없음).

## 2. 응답에서 중복된 `category` 칸 제거

- `MealPlanItem`: `category` 칸 삭제. 분류는 이미 `food.category`로 내려간다.
  서버 안에서 쓰던 `item.category()`는 `food.getCategory()`를 돌려주는 메서드로 남겨서 호출부는 그대로다 (JSON에는 안 나감).
- `MealPlanService`: 삭제된 칸에 값을 넘기던 부분 정리.
- `frontend/src/lib/meal-api.ts`: `MealPlanItem` 타입에서 `category` 삭제 (화면은 `categoryLabel`만 사용).

## 확인

- `./gradlew test` 전체 통과 (`MealBulkTest` 1개, `MealPlanServiceTest` 8개 포함)
- `frontend`에서 `tsc -b --noEmit` 통과
