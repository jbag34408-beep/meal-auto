# meal-auto 프로젝트 전체 정리 (2026-10-16 기준)

> 지금까지 한 작업 전부를 한 문서로 정리한 것.
> **어떤 도구를 썼는지**, **어떤 코드가 어디에 있고 무슨 일을 하는지**, **어떤 순서로 만들어 왔는지**를 담았다.
> 화면 작업의 세부 기록은 `docs/frontend-login-progress.md`, `docs/frontend-progress.md`, 디자인 브리프는 `docs/frontend-brief.md`에 따로 있다.

---

## 목차

1. [이 서비스는 무엇인가](#1-이-서비스는-무엇인가)
2. [사용한 도구와 기술 한눈에 보기](#2-사용한-도구와-기술-한눈에-보기)
3. [전체 구조 (요청이 흘러가는 길)](#3-전체-구조-요청이-흘러가는-길)
4. [폴더 구조](#4-폴더-구조)
5. [백엔드 코드 설명](#5-백엔드-코드-설명)
6. [프론트엔드 코드 설명](#6-프론트엔드-코드-설명)
7. [API 목록](#7-api-목록)
8. [데이터베이스와 배포](#8-데이터베이스와-배포)
9. [테스트](#9-테스트)
10. [작업 흐름 (커밋 순서)](#10-작업-흐름-커밋-순서)
11. [실행 방법](#11-실행-방법)
12. [환경 변수 정리](#12-환경-변수-정리)
13. [알아 두면 좋은 개념](#13-알아-두면-좋은-개념)

---

## 1. 이 서비스는 무엇인가

**식단 자동화 (meal-auto)** — 먹은 음식을 기록하면 서버가

- 칼로리·탄수화물·단백질·지방을 **계산**하고
- 회원마다 정한 **하루 목표와 비교**해서
- 남은 아침·점심·저녁에 먹을 **한식 상차림을 추천**하고
- 매일 밤 21시(한국 시간)에 **리포트를 자동으로 만들어 저장**하는 웹 서비스.

음식은 문장으로도 기록할 수 있다 ("점심에 김치찌개 한 그릇이랑 공기밥 반 공기") → **Gemini AI**가 음식별로 나눠 영양 값을 추정한다.
식약처 **공공데이터 API**에서 음식을 검색해 가져올 수도 있다.

주요 사용자는 과제·포트폴리오를 확인하는 사람(교수님·면접관)이라, "백엔드가 진짜로 계산하고 저장하고 추천한다"는 것이 화면에서 잘 보이도록 만들었다 (`PRODUCT.md`).
체험 계정: `demo` / `demo1234`

---

## 2. 사용한 도구와 기술 한눈에 보기

### 2-1. 백엔드 (서버)

| 도구 / 기술 | 버전 | 어디에 썼나 |
|---|---|---|
| **Java** | 17 | 서버 언어 |
| **Spring Boot** | 4.1.1 | 서버 프레임워크 (`build.gradle`) |
| Spring Web MVC | (Boot 포함) | REST API 컨트롤러 (`controller/`) |
| **Spring Data JPA** (Hibernate) | (Boot 포함) | DB 테이블 ↔ 자바 클래스 연결 (`domain/`, `repository/`) |
| **Spring Security** | (Boot 포함) | 로그인·회원가입, 세션, BCrypt 암호화, CSRF 보호 (`security/`) |
| Spring Validation | (Boot 포함) | 입력값 검사 `@Valid`, `@NotBlank`, `@Max` (`dto/`) |
| Spring Scheduling | (Boot 포함) | 매일 21시 리포트 `@Scheduled` (`scheduler/`) |
| RestClient | (Boot 포함) | 외부 API 호출 (식약처, Gemini) |
| Jackson 3 (`tools.jackson`) | (Boot 포함) | JSON 읽기·쓰기 |
| **H2 Database** | | 로컬용 파일 DB (`./data/mealdb`) |
| **PostgreSQL** 드라이버 | | 배포용 DB (Neon / Render PostgreSQL) |
| **Gradle** | wrapper | 빌드 도구 (`./gradlew bootRun`, `./gradlew test`) |
| JUnit 5 | | 테스트 (`src/test/`) |

### 2-2. 프론트엔드 (화면)

| 도구 / 기술 | 버전 | 어디에 썼나 |
|---|---|---|
| **React** | 19 | 화면 라이브러리 (`frontend/src/pages/`) |
| **TypeScript** | 5.9 | 타입이 있는 자바스크립트 |
| **Vite** | 8 | 개발 서버(5173) + 빌드 도구 (`frontend/vite.config.ts`) |
| **Tailwind CSS** | v4 | 클래스로 스타일 지정 (`@tailwindcss/vite`) |
| **shadcn/ui** 구조 | | 컴포넌트 폴더 규칙 (`components.json`, `components/ui/`) |
| Radix UI Slot | | 버튼 컴포넌트의 `asChild` 기능 |
| class-variance-authority, clsx, tailwind-merge | | 클래스 이름 조합 (`lib/utils.ts`의 `cn()`) |
| **three.js + @react-three/fiber + drei** | three 0.186 | 3D 액체 크롬 배경 (`components/aura/chrome-blob.tsx`) |
| **framer-motion** | 14 | 카드 등장·전환 애니메이션 |
| **gsap** | 3.15 | 소개 화면 푸터 애니메이션 (`components/ui/motion-footer.tsx`) |
| lucide-react | | 아이콘 |
| Pretendard (폰트) | | 한글 글꼴 |

### 2-3. 외부 서비스

| 서비스 | 용도 | 코드 |
|---|---|---|
| **Google Gemini API** (무료 등급) | 문장 → 음식별 영양 분석 | `service/ai/GeminiNutritionAnalyzer.java` |
| **식약처 식품영양성분 DB API** (공공데이터포털) | 음식 영양 정보 검색·가져오기 | `service/PublicFoodClient.java` |
| **Neon / Render PostgreSQL** | 배포 DB | `config/DatabaseUrl.java` |
| **Render** | 배포 (Docker) | `Dockerfile` |
| **GitHub** | 코드 저장소 (`jbag34408-beep/meal-auto`) | |

### 2-4. 개발·디자인 도구

| 도구 | 용도 |
|---|---|
| **Docker** | 배포 이미지 만들기 (3단계 빌드, `Dockerfile`) |
| Dev Container | GitHub Codespaces / VS Code 개발 환경 (`.devcontainer/devcontainer.json`) |
| IntelliJ IDEA / VS Code | 코드 편집 |
| **Claude Code** | AI 코딩 도우미 (설명 문서 `CLAUDE.md`, 스킬은 `.claude/` — git 제외) |
| **impeccable** (Claude 디자인 스킬) | 디자인 시스템·화면 리뷰 기록 (`.impeccable/`, `DESIGN.md`, `PRODUCT.md`) |
| **Figma** (+ Figma MCP) | 화면 시안 ("식단 자동화 - Aura 디자인") |
| **21st.dev** | 공개 React 컴포넌트 가져오기 (liquid-glass-button, sign-in-card-2, motion-footer) |

---

## 3. 전체 구조 (요청이 흘러가는 길)

```
[브라우저]
   │  React 화면 (landing.html / login.html / index.html)
   │  fetch("/meals", ...)  ← 세션 쿠키 + CSRF 헤더 자동 첨부 (lib/csrf.ts)
   ▼
[Spring Boot 서버 :8080]
   │ ① Spring Security 필터: 로그인했나? CSRF 토큰 맞나?
   │ ② Controller: 주소별로 요청 받기, 입력값 검사(@Valid)
   │ ③ Service: 실제 계산·추천·저장 로직
   │      ├─ NutritionCalculator (영양 계산)
   │      ├─ MealPlanService (한식 상차림 추천)
   │      ├─ GeminiNutritionAnalyzer ──▶ Google Gemini API
   │      └─ PublicFoodClient ─────────▶ 식약처 공공데이터 API
   │ ④ Repository (JPA): SQL 없이 DB 읽기·쓰기
   ▼
[DB]  로컬: H2 파일 / 배포: PostgreSQL

[DailyMealScheduler] 매일 21:00 KST → 회원마다 요약·추천 → daily_report 테이블에 저장
```

화면 파일도 Spring Boot가 같은 주소에서 내려 준다. `npm run build` 결과가 `src/main/resources/static/`으로 들어가기 때문.
→ 화면과 API가 **같은 주소(같은 출처)** 라서 로그인 세션 쿠키가 자연스럽게 붙는다.

---

## 4. 폴더 구조

```
meal-auto/
├── build.gradle, settings.gradle, gradlew   ← Gradle 빌드 설정
├── Dockerfile, .dockerignore                ← 배포 이미지
├── .devcontainer/devcontainer.json          ← 개발 컨테이너 (Java 17, 8080 포트)
├── README.md       ← API·DB·추천 방식 요약
├── PRODUCT.md      ← 누구를 위한 서비스인지 (impeccable)
├── DESIGN.md       ← 색·글꼴 등 디자인 시스템 (impeccable)
├── CLAUDE.md       ← Claude Code에게 주는 프로젝트 설명
├── docs/           ← 작업 정리 문서들 (이 문서 포함)
├── .impeccable/    ← 디자인 리뷰 기록
│
├── src/main/java/com/parkjonghwan/meal_auto/
│   ├── MealAutoApplication.java   ← 프로그램 시작점 (@SpringBootApplication, @EnableScheduling)
│   ├── DataInitializer.java       ← 서버 시작 때 기본 음식·demo 계정 넣기
│   ├── FoodSourceColumnFix.java   ← 예전 DB 열 제약 풀기
│   ├── config/       ← DB 주소 변환, 시간대, 기본 음식 CSV 읽기
│   ├── controller/   ← API 주소 (8개)
│   ├── domain/       ← DB 테이블이 되는 클래스 (Food, MealEntry, Member, DailyReport …)
│   ├── dto/          ← 요청·응답 모양 (record)
│   ├── exception/    ← 오류 종류 + 오류 응답 한 곳에서 처리
│   ├── repository/   ← DB 조회 인터페이스 (JPA)
│   ├── scheduler/    ← 매일 21시 리포트
│   ├── security/     ← 로그인·CSRF 설정
│   └── service/      ← 핵심 로직 (+ ai/ Gemini)
├── src/main/resources/
│   ├── application.properties   ← 설정 (환경 변수로 키·DB 주입)
│   ├── seed/foods.csv           ← 공용 기본 음식 68가지
│   └── static/                  ← 빌드된 화면이 들어가는 곳 (git 제외)
├── src/test/java/...            ← 테스트 12개 파일
│
└── frontend/                    ← React 화면
    ├── landing.html, login.html, index.html   ← 화면 3개의 입구
    ├── vite.config.ts           ← 빌드 결과를 ../src/main/resources/static 으로
    ├── package.json             ← 사용하는 npm 패키지
    └── src/
        ├── index.css            ← Tailwind + 공용 스타일
        ├── lib/                 ← 서버 호출(api.ts, meal-api.ts), CSRF(csrf.ts), cn()(utils.ts)
        ├── components/aura/     ← 공용 Aura 디자인 (3D 배경, 테마, 로그인 카드)
        ├── components/ui/       ← 버튼·입력칸·푸터 등 부품
        └── pages/
            ├── landing/   ← 소개 화면
            ├── login/     ← 로그인·회원가입
            └── meal/      ← 오늘 식단 (카드 5개)
```

---

## 5. 백엔드 코드 설명

### 5-1. domain — DB 테이블

`@Entity`가 붙은 클래스 하나 = 테이블 하나. `spring.jpa.hibernate.ddl-auto=update` 설정 덕분에 서버가 켜질 때 테이블이 자동으로 만들어진다.

| 클래스 | 테이블 | 내용 |
|---|---|---|
| `Member` | `members` | 아이디, BCrypt 비밀번호, 하루 목표(kcal 2000 / 탄 300 / 단 60 / 지 50 기본값) |
| `Food` | `food` | 음식 이름, **100g 기준** 영양 값, 출처(`FoodSource`), 1인분 g, 상차림 분류(`FoodCategory`), 주인(owner) |
| `MealEntry` | `meal_entry` | 누가 · 언제 · 어느 끼니에 · 어떤 음식을 · 몇 g 먹었나 |
| `DailyReport` | `daily_report` | 매일 밤 만든 리포트 (요약 + 추천) |
| `MealType` | (enum) | BREAKFAST / LUNCH / DINNER / SNACK |
| `FoodSource` | (enum) | SEED(기본) · PUBLIC(공공데이터) · USER(직접 등록) · AI(AI 추정) |
| `FoodCategory` | (enum) | RICE(밥) · SOUP(국·찌개) · MAIN(주반찬) · SIDE(나물·볶음) · KIMCHI |

**공용 음식 vs 개인 음식** (`Food.owner`)

```java
// 주인: 비어 있으면 모두가 쓰는 공용 음식(기본 음식·공공데이터),
// 있으면 그 회원만 보고 쓰는 개인 음식(직접 등록·AI 추정)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "member_id")
@JsonIgnore
private Member owner;

public boolean isShared() { return owner == null; }   // JSON에는 "shared": true/false
```

`FoodRepository.findVisibleTo(member)` = 공용 음식 + 내 음식만. 다른 회원이 등록한 음식은 목록·기록·추천 어디에도 나오지 않는다.

### 5-2. service — 핵심 로직

| 클래스 | 하는 일 |
|---|---|
| `NutritionCalculator` | 100g 기준 값 × (먹은 g ÷ 100) |
| `MealSummaryService` | 하루 기록을 모아 합계 (`DailySummary`) |
| `MealPlanService` | **남은 끼니별 한식 상차림 추천** (`/meal-plan`) |
| `MealRecommendService` | 가장 부족한 영양소를 채울 음식 후보 3개 (`/recommendation`, 밤 리포트) |
| `FoodService` | 음식 등록·목록, 공공데이터 음식 가져오기 |
| `PublicFoodClient` | 식약처 API 호출 (연결 5초, 응답 10초 제한) |
| `MealAnalysisService` | AI 분석 결과를 검사 → 트랜잭션으로 한 번에 저장 |
| `ai/NutritionAnalyzer` | AI 분석기 인터페이스 (테스트에서는 `FakeNutritionAnalyzer`로 바꿔 끼움) |
| `ai/GeminiNutritionAnalyzer` | Gemini 호출, 재시도, 예비 모델 전환 |
| `MemberService` | 회원가입 (BCrypt 암호화), 목표 변경 |
| `DailyReportService` | 리포트 저장 (같은 날 있으면 갱신) |

#### 영양 계산 (`NutritionCalculator`)

```java
private double scale(double per100g, double grams) {
  return per100g * (grams / 100);
}
public double calculateKcal(MealEntry entry) {
  return scale(entry.getFood().getKcalPer100g(), entry.getGrams());
}
```

#### 한식 상차림 추천 (`MealPlanService`) — 가장 공들인 부분

1. 오늘 목표 − 이미 먹은 양 = **남은 영양**
2. 아직 기록 안 한 끼니(오늘이면 지난 시간대 제외: 10시 전 아침부터, 15시 전 점심부터, 21시 이후 없음)에 나눔
   - 비율: 아침 25% · 점심 40% · 저녁 35%
   - 한 끼 상한 = 기본 몫 × 1.25 (놓친 끼니를 한 끼에 몰아 먹지 않게)
3. 끼니마다 **밥 × 국 × 주반찬 × 나물 × 김치의 모든 조합**을 계산해 목표와 가장 가까운 것을 고름
   - 밥: 0.5 / 0.75 / 1 / 1.25 공기, 주반찬: 1 / 1.5인분
   - 아침은 나물 없이
   - 같은 날 국·주반찬·나물은 겹치지 않게, 최근 3일 안에 먹은 메뉴는 점수 0.4 감점
4. 점수(오차) = 영양소별 `(목표와의 차이 ÷ 목표)²`의 합
   - 칼로리는 2배 중요, 목표를 넘는 쪽은 1.3배 더 나쁘게 (단백질은 넘쳐도 0.3배만)

```java
static double error(Nutrition actual, Nutrition target) {
  return sq(actual.carbohydrate(), target.carbohydrate(), CARB_FLOOR, OVER_WEIGHT)
      + sq(actual.protein(), target.protein(), PROTEIN_FLOOR, PROTEIN_OVER_WEIGHT)
      + sq(actual.fat(), target.fat(), FAT_FLOOR, OVER_WEIGHT)
      + KCAL_WEIGHT * sq(actual.kcal(), target.kcal(), KCAL_FLOOR, OVER_WEIGHT);
}
```

메뉴는 `seed/foods.csv`에서 상차림 분류가 있는 음식 (밥 4 · 국 8 · 주반찬 12 · 나물 11 · 김치 2).

#### 부족 영양소 추천 (`MealRecommendService`)

목표 대비 **비율로** 가장 부족한 영양소를 찾고, 음식마다 "그 부족분을 채우는 양"을 계산해 점수를 매긴다.

```
점수 = 채운 정도 − 초과한 정도 − 칼로리 부담(×0.3) − 최근 먹은 음식(0.25)
양 제한 = min(500g, 1인분×2, 하루 목표 kcal의 40%, 남은 kcal)
```

점수 순 최대 3개 반환. 밤 21시 리포트에도 이 추천이 들어간다.

#### Gemini AI 분석 (`GeminiNutritionAnalyzer`)

- 시스템 프롬프트로 "한국 음식 영양 분석기" 역할을 주고, **JSON 스키마**(`responseJsonSchema`)로 응답 모양을 강제
  → `{ foods: [{ name, grams, kcalPer100g, carbohydrate, protein, fat }] }`
- 키는 `x-goog-api-key` 헤더로, **결제 계정 없는 무료 키만** 사용 (비용 0)
- 오류 처리
  - 500/503/504 (일시 오류) → 1초 뒤 같은 모델로 1번 재시도
  - 429(한도 초과)/404/일시 오류 반복 → **예비 모델로 자동 전환** (`gemini.fallback-models`)
  - 전부 실패 → `AnalyzerUnavailableException` → 화면에 "직접 입력해 주세요" 안내(503)
- 로그에 키 값은 절대 남기지 않음 (있는지와 길이만)

`MealAnalysisService`는 AI 결과를 검사한 뒤 `TransactionTemplate`으로 **전부 저장하거나 전부 취소**한다. 이미 있는 이름의 음식은 재사용하고, 없으면 AI 출처의 개인 음식으로 새로 만든다.

#### 공공데이터 (`PublicFoodClient`, `FoodService`)

- 응답 필드 이름(`FOOD_NM_KR`, `AMT_NUM1` …)을 상수로 모아 둬서 바뀌면 한 곳만 고치면 됨
- 가져오기(`POST /foods/public`) 때 **화면이 보낸 영양 값은 믿지 않고** 서버가 같은 이름으로 다시 조회한 값만 저장
- 키(`FOOD_API_KEY`)가 없으면 `/foods/public/status`가 꺼짐을 알려 주고 화면은 안내 문구만 보여 줌

### 5-3. controller — API 주소

| 클래스 | 주소 |
|---|---|
| `HomeController` | `/` → 소개 화면 |
| `CsrfController` | `GET /csrf` (CSRF 토큰 쿠키 발급) |
| `MemberController` | `POST /signup`, `GET /me`, `PUT /me/targets` |
| `FoodController` | `/foods`, `/foods/public`, `/foods/public/status` |
| `MealController` | `/meals`, `/meals/bulk`, `/meals/{id}`, `/summary`, `/recommendation` |
| `MealPlanController` | `/meal-plan` |
| `MealAnalysisController` | `POST /meals/analyze` (AI 문장 기록) |
| `ReportController` | `GET /reports` (기간별 리포트) |

컨트롤러는 `Principal`(로그인한 사람의 아이디)로 회원을 찾아서(`memberService.get(principal.getName())`), **항상 그 회원의 데이터만** 다룬다.

### 5-4. security — 로그인과 보안 (`SecurityConfig`)

```java
http
  .csrf(csrf -> csrf.spa())     // XSRF-TOKEN 쿠키 → X-XSRF-TOKEN 헤더로 확인
  .authorizeHttpRequests(auth -> auth
      .requestMatchers("/", "/landing.html", "/login.html", "/assets/**",
                       "/signup", "/login", "/csrf", "/error", "/favicon.ico").permitAll()
      .anyRequest().authenticated())
  .formLogin(form -> form
      .loginPage("/login.html")
      .loginProcessingUrl("/login")
      .successHandler((req, res, auth) -> res.setStatus(200))   // 페이지 이동 대신 상태 코드
      .failureHandler((req, res, ex) -> res.setStatus(401)))
```

- 비밀번호는 `BCryptPasswordEncoder`로 암호화해서 저장
- 세션 쿠키 방식 로그인 → 다른 사이트가 몰래 요청을 보내는 걸 막으려고 **CSRF 보호**를 켬
- 로그인 안 된 요청: 화면 요청이면 `/login.html`로 이동, API 요청이면 401
- `MemberDetailsService`: DB의 회원을 Spring Security가 이해하는 형태로 바꿔 줌
- 응답 JSON에 회원 정보(비밀번호 해시)가 섞이지 않도록 `@JsonIgnore` 처리

### 5-5. exception — 오류 처리 (`GlobalExceptionHandler`)

`@RestControllerAdvice` 하나에서 모든 예외를 상태 코드 + 한국어 메시지로 바꾼다.

| 상황 | 상태 코드 |
|---|---|
| 입력값 검사 실패, JSON 오류, 영양 값 조합 이상 | 400 |
| 없는 음식 / 공공데이터에 없는 음식 | 404 |
| 같은 이름 음식·아이디 | 409 |
| AI가 음식을 못 찾음 | 422 |
| 외부 API 실패 | 502 |
| AI 사용 불가 | 503 |

### 5-6. scheduler — 매일 자동 리포트

```java
@Scheduled(cron = "0 0 21 * * *", zone = "Asia/Seoul")
public void dailyReport() {
  for (Member member : memberRepository.findAll()) {
    DailySummary summary = summaryService.summarizeDay(member, today);
    Recommendation recommendation = recommendService.recommend(summary, member);
    reportService.generateAndSave(member, today, summary, recommendation);
  }
}
```

### 5-7. config · 시작 시 실행되는 코드

| 파일 | 하는 일 |
|---|---|
| `TimeConfig` | `Clock`을 **Asia/Seoul**로 등록 → 서버가 UTC여도 "오늘"은 한국 날짜. 테스트에서는 시각 고정 가능 |
| `DatabaseUrl` | `postgres://아이디:비번@호스트/DB` 주소를 JDBC 주소 + 아이디 + 비번으로 나눔 |
| `DatabaseStorageCheck` | Render에서 H2를 쓰고 있으면 경고 (재시작 때 데이터가 지워지므로) |
| `SeedFoodCatalog` | `seed/foods.csv` 읽기 |
| `DataInitializer` | ① DB에 없는 기본 음식만 추가 ② demo 계정과 샘플 기록 ③ 예전 데이터의 주인 정리 — 여러 번 실행해도 결과 같음 |
| `FoodSourceColumnFix` | 예전에 만들어진 `food.source`·`category` 열의 enum 제약을 풀어 새 값 저장 가능하게 |

---

## 6. 프론트엔드 코드 설명

### 6-1. 빌드 방식 (`frontend/vite.config.ts`)

- 화면 3개(`login`, `index`, `landing`)를 각각 입구로 빌드 (멀티 페이지)
- 결과를 `../src/main/resources/static`에 출력 → Spring Boot가 그대로 서비스
- 개발 중(`npm run dev`, 5173)에는 `/meals`, `/foods` 같은 API 요청을 8080으로 **프록시**
- `@` = `frontend/src` 별칭 (`import { cn } from "@/lib/utils"`)

### 6-2. 서버 연결 (`frontend/src/lib/`)

| 파일 | 내용 |
|---|---|
| `csrf.ts` | `secureFetch()`: POST·PUT·DELETE 요청에 `XSRF-TOKEN` 쿠키 값을 `X-XSRF-TOKEN` 헤더로 붙임. 쿠키가 없으면 `/csrf`로 먼저 받아 옴 |
| `api.ts` | 로그인(폼 형식), 회원가입(JSON), 서버 오류 메시지 꺼내기 |
| `meal-api.ts` | 식단 화면의 모든 API 호출과 응답 타입 |
| `utils.ts` | `cn()` — clsx + tailwind-merge로 클래스 합치기 (shadcn 기본) |

```ts
export async function secureFetch(url: string, init: RequestInit = {}) {
  const method = (init.method ?? "GET").toUpperCase()
  if (SAFE_METHODS.has(method)) return fetch(url, init)
  const headers = new Headers(init.headers)
  headers.set(HEADER, await csrfToken())
  return fetch(url, { ...init, headers })
}
```

### 6-3. 공용 Aura 디자인 (`components/aura/`)

세 화면이 모두 같은 디자인: **3D 액체 크롬 배경 + 반투명 유리 카드 + 알약 모양 버튼 + 라이트/다크 테마**.

| 파일 | 내용 |
|---|---|
| `aura-backdrop.tsx` | 화면 배경. CSS 그라데이션을 먼저 깔고 three.js 장면은 **필요할 때 따로 받아 옴**(lazy). 3D가 실패해도 배경은 남음 |
| `chrome-blob.tsx` | react-three-fiber로 그린 금속 구. **simplex noise**로 표면 정점을 굽이치게 밀어 액체처럼 움직임 |
| `theme-toggle.tsx`, `use-aura-theme.ts` | 라이트/다크 전환, `localStorage["aura-theme"]`로 화면끼리 공유 |
| `auth-card.tsx` | 로그인 ↔ 가입을 한 카드에서 전환 |
| `brand-icons.tsx` | 소셜 로그인 로고 (서버 기능은 아직 없음, 안내만) |
| `aura.css` | Aura 색 변수(`--aura-*`)와 유리 효과 |

`components/ui/`: `liquid-glass-button.tsx`, `sign-in-card.tsx`(21st.dev sign-in-card-2), `input.tsx`(shadcn), `motion-footer.tsx`(21st.dev, gsap, Aura 색으로 수정).

### 6-4. 화면별 (`pages/`)

**소개 (`pages/landing`)** — 스크롤 없는 한 화면, "체험 계정으로 시작" 버튼이 demo로 바로 로그인.

**로그인 (`pages/login`)** — `?mode=signup`이면 가입 카드부터. 성공하면 `/index.html`로.

**오늘 식단 (`pages/meal`)**

| 파일 | 카드 |
|---|---|
| `MealPage.tsx` | 화면 전체, 날짜 선택, 데이터 다시 불러오기 |
| `SummaryCard.tsx` | 하루 요약 — 숫자가 이전 값에서 새 값으로 굴러가는 애니메이션, `+120 / −80` 변화 표시 |
| `MealPlanCard.tsx` | 끼니별 한식 상차림 추천, 목표 대비 % 막대, "이대로 기록" (`/meals/bulk`) |
| `LogCard.tsx` | "무엇을 먹었나요?" — AI 문장 기록 + 직접 고르기. AI가 안 되면 직접 고르기를 펼쳐 안내 |
| `RecordsCard.tsx` | 끼니별로 묶은 기록 + 끼니별 칼로리, 삭제 |
| `ManageCard.tsx` | 하루 목표 / 공공데이터 검색 / 내 음식 등록 (접었다 펴는 알약 줄) |
| `parts.tsx` | 유리 카드, 끼니 선택 라디오 등 작은 부품 |

---

## 7. API 목록

(🔒 = 로그인 필요, 데이터를 바꾸는 요청은 CSRF 토큰 필요)

| 메서드 | 주소 | 설명 |
|---|---|---|
| GET | `/csrf` | CSRF 토큰 쿠키 받기 |
| POST | `/signup` | 회원가입 `{"username","password"}` |
| POST | `/login` | 로그인 (폼 형식) |
| POST | `/logout` | 로그아웃 |
| GET 🔒 | `/me` | 내 정보·목표 |
| PUT 🔒 | `/me/targets` | 하루 목표 변경 |
| GET 🔒 | `/foods` | 음식 목록 (공용 + 내 음식) |
| POST 🔒 | `/foods` | 내 음식 등록 (100g 기준) |
| GET 🔒 | `/foods/public/status` | 공공데이터 사용 가능 여부 |
| GET 🔒 | `/foods/public?keyword=` | 식약처 DB 검색 (저장 안 함) |
| POST 🔒 | `/foods/public` | 식약처 음식을 공용 음식으로 가져오기 |
| GET 🔒 | `/meals?date=` | 그날 기록 |
| POST 🔒 | `/meals` | 기록 추가 `{"mealType","foodId","grams"}` |
| POST 🔒 | `/meals/bulk` | 상차림 한 번에 기록 |
| POST 🔒 | `/meals/analyze` | AI 문장 기록 |
| DELETE 🔒 | `/meals/{id}` | 기록 삭제 (내 것만) |
| GET 🔒 | `/summary?date=` | 하루 요약 |
| GET 🔒 | `/meal-plan?date=` | 남은 끼니 한식 상차림 추천 |
| GET 🔒 | `/recommendation?date=` | 부족 영양소 음식 추천 (후보 3개) |
| GET 🔒 | `/reports?from=&to=` | 기간별 매일 리포트 |

---

## 8. 데이터베이스와 배포

### DB

| 환경 | DB | 설정 |
|---|---|---|
| 로컬 | H2 파일 DB `./data/mealdb` | 기본값 (아무것도 안 넣으면) |
| 배포 | 외부 PostgreSQL (Neon 추천 / Render PostgreSQL) | `DB_URL` 환경 변수 |

- 처음에는 H2 → Neon PostgreSQL로 분리 → Render PostgreSQL도 쓸 수 있게 주소 변환(`DatabaseUrl`) 추가
- Render 무료 인스턴스는 재시작 때 디스크가 지워져서 배포에서 H2를 쓰면 데이터가 사라짐 → 경고 로그
- SQL 로그는 기본 꺼짐 (`SHOW_SQL=true`로 켬)

### 배포 (`Dockerfile`, 3단계)

```dockerfile
# 1단계: Node 22로 React 화면 빌드 → static 폴더
FROM node:22-slim AS web
RUN npm ci && npm run build

# 2단계: JDK 17로 jar 만들기 (1단계 화면 포함, gradlew 줄바꿈 CRLF→LF 처리)
FROM eclipse-temurin:17-jdk AS build
RUN ./gradlew bootJar -x test

# 3단계: 가벼운 JRE에 jar만 복사해서 실행 (한국 시간대)
FROM eclipse-temurin:17-jre
ENV TZ=Asia/Seoul
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
```

빌드된 화면 파일(`landing.html`, `login.html`, `index.html`, `assets/`)은 git에 올리지 않고 배포 때 Docker가 만든다.

---

## 9. 테스트

`./gradlew test`로 실행 (JUnit 5).

| 테스트 | 확인하는 것 |
|---|---|
| `NutritionCalculatorTest` | g 환산 계산 |
| `MealSummaryServiceTest` | 하루 합계 |
| `MealRecommendServiceTest` | 부족 영양소 판단, 양 제한, 점수 순서 |
| `MealPlanServiceTest` | 끼니 나누기, 지난 끼니 제외, 메뉴 겹침 방지 등 |
| `FoodServiceTest` | 공용/개인 음식, 중복 이름, 공공데이터 가져오기 |
| `MealAnalysisServiceTest` | AI 결과 검사, 한 번에 저장/취소 (`FakeNutritionAnalyzer` 사용) |
| `GeminiNutritionAnalyzerTest` | 모델 순서, 재시도/다음 모델 판단, 응답 해석 |
| `DailyReportServiceTest` | 리포트 저장·갱신 |
| `DataInitializerTest` | 기본 음식 추가, 여러 번 실행해도 같은 결과 |
| `DatabaseUrlTest` | `postgres://` → JDBC 변환 |
| `SeedFoodCatalogTest` | CSV 읽기 |

외부 API는 인터페이스(`NutritionAnalyzer`) 뒤에 숨겨 두고 테스트에서는 가짜로 바꿔 끼워, **인터넷 없이** 테스트가 돈다.

---

## 10. 작업 흐름 (커밋 순서)

전체 커밋 44개. 큰 흐름만 단계별로 묶었다.

### 1단계 — 첫 버전 (백엔드 뼈대)
- 식단 자동화 서비스 첫 버전: 영양 계산, 하루 요약, 추천, 매일 자동 리포트
- 운영 DB를 PostgreSQL(Neon)로 분리, 환경 변수로 설정
- **식약처 식품영양성분 API 연동** (검색·가져오기)
- Dev Container 설정, 외부 API 시간 제한(연결 5초·응답 10초)

### 2단계 — 회원 기능
- **로그인·회원가입**, 회원별 기록과 목표, 회원별 자동 리포트
- 추천 근거 응답(왜 이 음식인지) + 추천 근거 패널·재계산 모션, 3D 랜딩 페이지
- 제품·디자인 문서(PRODUCT.md, DESIGN.md) 추가
- `/` 접속 시 랜딩, 로그인 후 식단 화면
- **매일 리포트 DB 저장** + 기간 조회 API

### 3단계 — AI 기록
- **Gemini 무료 등급으로 문장 식사 기록**, 식단 화면에 입력창
- 503 원인 로그, 일시 오류 최대 2번 재시도, 기본 모델이 붐비면 **예비 모델로 자동 전환**

### 4단계 — 화면 디자인 개편 → React 이전
- 앱·로그인 화면을 "세 가닥" 디자인으로 개편, 액체 입자 효과 실험 후 되돌림
- `bootRun`이 화면 파일을 src에서 바로 읽게 설정
- **React 프론트엔드 도입** (Vite + TS + Tailwind v4 + shadcn) — 로그인 화면부터
- 로그인 화면 디자인 여러 번 교체 → 최종 **Aura 디자인**(3D 크롬 배경, 유리 카드, 라이트/다크)
- **식단 화면**, **소개 화면**을 React + Aura로 이전 → 모든 화면이 React
- 소개 화면을 스크롤 없는 한 화면으로 정리
- 작업 정리 문서 3개 (`docs/`)

### 5단계 — 데이터·보안·추천 고도화 (최근)
- 음식을 **공용(SEED·공공데이터) / 개인(직접 등록·AI)** 으로 분리
- 배포 DB를 외부 PostgreSQL로 옮길 수 있게 정리 (`DatabaseUrl`, `DatabaseStorageCheck`)
- **CSRF 보호 켜기**, 날짜를 **한국 시간 기준**으로, SQL 로그 기본 끄기
- 추천을 **점수 방식**으로 바꾸고 후보 3개 표시
- 공용 기본 음식 **68가지**로 확대 (CSV), 추천량을 1인분 기준으로 제한
- 식단 추천을 **끼니별 한식 상차림**으로 변경 (`/meal-plan`, `/meals/bulk`)

---

## 11. 실행 방법

```bash
# 1) 백엔드 (Java 17 필요) → http://localhost:8080
./gradlew bootRun

# 2) 화면 개발 서버 → http://localhost:5173 (API는 8080으로 프록시)
cd frontend
npm install        # 처음 한 번
npm run dev

# 배포용 화면 빌드 → src/main/resources/static 에 생성 (8080에서 바로 보려면 필요)
npm run build

# 테스트
./gradlew test

# Docker로 통째로
docker build -t meal-auto .
docker run -p 8080:8080 -e GEMINI_API_KEY=... meal-auto
```

로그인: `demo` / `demo1234`

---

## 12. 환경 변수 정리

| 이름 | 기본값 | 설명 |
|---|---|---|
| `DB_URL` | `jdbc:h2:file:./data/mealdb` | DB 주소 (`postgres://…` 그대로 넣어도 됨) |
| `DB_USERNAME` / `DB_PASSWORD` | `sa` / 빈 값 | `jdbc:` 주소를 쓸 때 |
| `FOOD_API_KEY` | 없음 | 식약처 공공데이터 키 (없으면 검색 꺼짐) |
| `GEMINI_API_KEY` | 없음 | Gemini 키 (없으면 AI 기록 꺼짐, **결제 계정 연결 금지**) |
| `GEMINI_MODEL` | `gemini-3.8-flash` | 기본 모델 |
| `GEMINI_FALLBACK_MODELS` | `gemini-3.5-flash,gemini-3.5-flash-lite` | 예비 모델 순서 |
| `SHOW_SQL` | `false` | SQL 로그 |
| `PORT` | `8080` | 서버 포트 |

키는 코드에 쓰지 않고 **환경 변수로만** 넣는다.

---

## 13. 알아 두면 좋은 개념

| 개념 | 이 프로젝트에서 |
|---|---|
| **계층 구조** (Controller → Service → Repository) | 주소 받기 / 로직 / DB를 나눠서 각자 한 가지 일만 |
| **JPA / Entity** | SQL을 직접 안 쓰고 자바 클래스로 테이블 다루기 |
| **DTO (record)** | 요청·응답 모양을 따로 정의 → DB 구조가 그대로 밖에 나가지 않음 |
| **의존성 주입 (DI)** | 생성자로 서비스를 받음 → 테스트에서 가짜로 바꿔 끼우기 쉬움 |
| **트랜잭션** | AI 기록 여러 개를 전부 저장하거나 전부 취소 |
| **세션 로그인 + CSRF** | 쿠키로 로그인 유지, 다른 사이트의 위조 요청은 토큰으로 막기 |
| **BCrypt** | 비밀번호를 되돌릴 수 없게 암호화 |
| **Clock 주입** | "지금 시각"을 바꿔 끼울 수 있게 → 한국 시간 고정, 테스트에서 시각 고정 |
| **멱등성** | `DataInitializer`처럼 여러 번 실행해도 결과가 같게 |
| **재시도 + 폴백** | Gemini 일시 오류는 재시도, 계속 안 되면 다른 모델 |
| **SPA처럼 동작하는 멀티 페이지** | 화면 3개는 각자 HTML, 데이터는 fetch로 받아 React가 그림 |
| **Lazy loading** | 무거운 three.js는 필요할 때만 받아 첫 화면을 빠르게 |
| **멀티 스테이지 Docker** | 빌드 도구는 빌드에만, 실행 이미지는 JRE + jar만 |
