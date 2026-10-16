# meal-auto

Spring Boot로 만든 식단 자동화 서비스예요. 영양 계산, 하루 요약, 끼니별 한식 상차림 추천을 지원해요.

## 기술
Java 17 · Spring Boot 4.1.1 · Spring Data JPA · PostgreSQL (배포) / H2 파일 DB (로컬) · React + Vite (화면)

## 실행
1. IntelliJ에서 `File → Open`으로 이 폴더를 열어요 (Gradle 프로젝트로 인식).
2. `MealAutoApplication`을 실행해요.

## 데이터베이스
| 환경 | DB | 설정 |
|---|---|---|
| 로컬 | H2 파일 DB (`./data/mealdb`) | 아무것도 안 넣으면 기본값 |
| 배포 (Render) | 외부 PostgreSQL (Neon 또는 Render PostgreSQL) | 환경 변수 `DB_URL` |

Render 무료 인스턴스는 재배포·재시작 때 디스크가 초기화돼서, 배포에서 H2 파일 DB를 쓰면 회원과 기록이 매번 사라져요.
그래서 배포에서는 외부 PostgreSQL을 써요. (Render에서 H2로 켜지면 서버 로그에 경고가 나와요)

### 배포 DB 연결 방법
1. DB 만들기
   - **Neon** (추천, 무료 플랜은 기간 제한 없음): 프로젝트를 만들고 `Connection string`을 복사
   - **Render PostgreSQL**: 서비스와 같은 지역에 만들고 `Internal Database URL`을 복사 (무료 DB는 만든 뒤 일정 기간이 지나면 만료되니 Render 안내 확인)
2. Render 웹 서비스 → `Environment`에 `DB_URL` = 복사한 주소 (`postgres://…` 또는 `postgresql://…` 그대로)
   - 서버가 시작할 때 `jdbc:postgresql://…` 주소와 아이디·비밀번호로 나눠서 써요 (`config/DatabaseUrl`)
   - `jdbc:postgresql://…` 형식으로 넣고 `DB_USERNAME`, `DB_PASSWORD`를 따로 넣어도 돼요
3. 재배포 → 테이블은 처음 실행 때 자동으로 만들어지고 기본 음식과 demo 계정도 자동으로 생겨요

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
| GET | `/meal-plan?date=` | 남은 끼니(아침·점심·저녁) 한식 상차림 추천 |
| POST | `/meals/bulk` | 상차림을 한 번에 기록 `{"mealType":"LUNCH","items":[{"foodId":1,"grams":210}]}` |

## 음식 데이터 (공용 · 개인)
- 공용: 기본 음식(SEED), 식약처 공공데이터(PUBLIC) → 모든 회원이 목록·기록·추천에 씀
  - 기본 음식 목록: `src/main/resources/seed/foods.csv` (곡류·고기·생선·콩·유제품·견과·과일·채소 68가지, 100g 기준 영양 값과 1인분 g)
  - 서버가 시작할 때 DB에 없는 이름만 추가해요 (이미 있는 음식 값은 그대로) → 목록에 줄을 추가하고 배포하면 운영 DB에도 들어가요
  - 식약처 공공데이터는 외부 API 대신 DB 테이블(`public_food`)에 내장해서 검색해요 (비어 있으면 화면에 "지금은 사용할 수 없어요")
    1. 한국에 있는 PC에서 받기 (data.go.kr이 해외 IP를 막음): `FOOD_API_KEY=<일반 인증키> node scripts/export-public-foods.js public-foods.tsv`
    2. 넣을 DB를 `DB_URL`로 잡고 넣기 (기존 내용은 지우고 새로, 끝나면 서버가 꺼짐): `./gradlew bootRun --args="--public-food.import=public-foods.tsv"`
- 개인: 직접 등록(USER), AI 추정(AI) → 만든 회원만 씀 (`food.member_id`), 다른 회원의 목록·추천에 섞이지 않음

## 식단 추천 (한식 상차림)
- 오늘 남은 칼로리·탄수화물·단백질·지방을 아직 안 먹은 끼니에 나눠요 (아침 25% · 점심 40% · 저녁 35%, 한 끼는 기본 몫의 1.25배까지)
- 끼니마다 밥 + 국·찌개 + 주반찬 + 나물·볶음 + 김치 조합을 모두 맞춰 보고 목표에 가장 가까운 상차림을 골라요 (아침은 나물 없이)
  - 밥은 반 공기 ~ 1과 1/4 공기, 주반찬은 1~1.5인분으로 양을 조절
  - 같은 날 국·주반찬·나물이 겹치지 않고, 최근 3일 안에 먹은 메뉴는 덜 골라요
- 메뉴는 기본 음식 목록(`seed/foods.csv`)의 상차림 분류(RICE·SOUP·MAIN·SIDE·KIMCHI)가 있는 음식이에요

## 패키지 구조
```
com.parkjonghwan.meal_auto
├── MealAutoApplication, DataInitializer
├── controller   MealController
├── domain       Food, MealEntry, MealType
├── dto          DailySummary, MealEntryRequest, MealPlan, Nutrition
├── repository   FoodRepository, MealEntryRepository
└── service      MealPlanService, MealSummaryService
```
