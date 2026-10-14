# 식단 자동화 (meal-auto) — 프론트엔드 디자인 브리프

> 디자인 도구(Variant 등)에 넘기고, 나온 디자인을 실제 코드로 옮길 때 기준으로 쓰는 문서.
> 맨 아래 **"디자인 도구에 넣을 프롬프트"**를 그대로 복사해서 쓰면 된다.

---

## 1. 서비스 한 줄 소개

먹은 음식을 적으면 **탄수화물·단백질·지방과 칼로리를 자동으로 계산**하고, 목표 대비 **가장 부족한 영양소를 채울 다음 끼니를 추천**하는 웹 서비스.
"김치찌개 한 그릇이랑 밥 반 공기"처럼 **문장으로 적으면 AI가 음식별로 나눠 기록**해 준다.

- 사용자: 식단을 관리하고 싶은 20~30대 (운동, 다이어트, 건강 관리)
- 사용 상황: 끼니 직후 휴대폰으로 빠르게 기록, 저녁에 하루를 돌아보며 다음 끼니 결정
- 목적: 포트폴리오용 실서비스 (배포 주소 https://meal-auto.onrender.com, 체험 계정 demo / demo1234)

## 2. 브랜드 방향

- 핵심 상징: **"하루 식단은 세 가닥"**. 탄수화물·단백질·지방을 세 가닥의 선·띠로 표현한다.
- 세 가닥 색 (모든 화면에서 같은 값)

  | 영양소 | 기본색 | 어두운 바탕 위 글자색 |
  |---|---|---|
  | 탄수화물 | `#d9a441` | `#e2b25a` |
  | 단백질 | `#2f6f4f` | `#7fbf9c` |
  | 지방 | `#b06a3b` | `#d98e5f` |

- 바탕·글자: 짙은 배경 `#0f1311`, 밝은 배경 `#eef1ec`, 본문 글자 `#18201b`, 보조 글자 `#5b655f`, 구분선 `#d5dbd4`
- 질감: **하얀 반투명 유리(Glass)**. 로그인 화면에 먼저 적용함 (반투명 흰 카드 + 흐림 효과 + 밝은 테두리)
- 버튼: **Liquid Glass**. 알약 모양의 반투명 흰 유리에 위쪽 안쪽 그늘, 아래쪽 짙은 굴절선, 부드러운 바깥 그림자, 검정 글자
- 글꼴: Pretendard (한국어 화면)
- 피할 것: 보라색 그라데이션, 흔한 SaaS 카드 나열, 모든 섹션이 같은 모양의 둥근 카드

## 3. 기술 조건 (디자인 결과물을 코드로 옮길 때)

- **React 19 + TypeScript + Tailwind CSS v4 + shadcn/ui 구조** (Vite). Next.js가 아님 → `next/link`, `next/image` 같은 Next 전용 기능은 쓰지 않는다.
- 쓸 수 있는 라이브러리: `framer-motion`(애니메이션), `lucide-react`(아이콘)
- 공용 컴포넌트 위치: `frontend/src/components/ui/` (이미 있음: `liquid-glass-button.tsx`, `input.tsx`, `sign-in-card.tsx`)
- 백엔드는 Spring Boot. 화면은 아래 API만 호출한다 (같은 주소, 로그인 세션 쿠키 방식).
- 외부 이미지 대신 CSS·SVG로 그린다. 스톡 사진은 쓰지 않는다.
- 반응형: 모바일(390px)을 먼저, 데스크톱(1280px)까지
- 접근성: 키보드 포커스가 보일 것, 글자 대비 충분할 것, "동작 줄이기" 설정이면 애니메이션 끌 것

## 4. 화면별 구현 범위

현재 상태: ✅ 완료 / 🟡 일반 HTML로만 있음(React로 다시 만들어야 함) / ⬜ 아직 없음

### 4-1. 로그인 · 회원가입 ✅
- 하얀 유리 카드, 마우스를 따라 기울어지는 3D 효과, 테두리 빛줄기
- 아이디/비밀번호 입력, 비밀번호 보기, 로그인 ↔ 회원가입 전환, "체험 계정으로 들어가기"
- 디자인을 새로 받으면 이 화면과 같은 유리 질감으로 맞춘다

### 4-2. 오늘 화면 (메인 대시보드) 🟡 — 가장 중요
하루에 몇 번씩 여는 화면. **기록이 가장 빨라야 한다.**

1. **상단 요약**
   - 날짜 이동 (‹ 전날 / 날짜 선택 / 다음 날 ›, 오늘 이후는 못 고름)
   - 오늘 섭취 칼로리 (큰 숫자) + 목표 칼로리, 기록한 끼니 수
   - 탄·단·지 세 가닥 진행 막대: `먹은 양 / 목표 g`
   - 기록이 바뀌면 숫자가 굴러가고 `+190` 같은 증감 칩이 잠깐 뜸
2. **"무엇을 먹었나요?" AI 기록** (화면에서 가장 눈에 띄어야 함)
   - 한 줄 입력 (최대 300자, 예: "김치찌개 한 그릇이랑 밥 반 공기")
   - 끼니 선택 버튼: 아침 / 점심 / 저녁 / 간식 (시간대에 맞게 기본 선택)
   - "AI로 기록" 버튼 → 분석 중 상태 → 성공 시 "점심으로 김치찌개 300g, 공기밥 105g을 기록했어요"
   - 안내 문구: "Google Gemini 무료 등급으로 양과 영양소를 추정해요"
   - AI를 못 쓸 때(서버 503): 오류 문구 + 아래 "직접 고르기"를 자동으로 펼침
   - 음식을 못 알아봤을 때(422): "음식 이름과 양을 조금 더 구체적으로 적어 주세요"
3. **직접 고르기** (접혀 있음): 등록된 음식 선택 + 끼니 + 그램 + 추가
4. **기록 목록**: 아침·점심·저녁·간식별로 묶고 끼니마다 칼로리 합계. 항목마다 음식명, 그램, 칼로리, 삭제. AI가 만든 음식에는 `AI 추정` 표시
5. **다음 끼니 추천**
   - "닭가슴살 174g" + 190kcal + 이유("단백질이 가장 부족해요")
   - 계산 근거 3단계: ① 목표 대비 부족한 영양소 표 (부족량, 달성률) ② 그 영양소가 가장 많은 음식 ③ `40.1g ÷ 23g × 100 = 174g`
   - 남은 칼로리에 맞춰 양을 줄였으면 그 안내
   - 끼니 선택 + "이대로 기록" 버튼
   - 추천 영역의 강조색은 채워 줄 영양소의 가닥 색을 따른다
   - 추천이 없을 때 문구 3가지: 칼로리 다 채움 / 세 영양소 다 채움 / 추천할 음식 없음
6. **목표와 음식 관리** (접혀 있음)
   - 내 하루 목표: 칼로리, 탄수화물, 단백질, 지방 → 저장
   - 식약처 데이터에서 음식 찾기: 검색 → 결과 표(음식, 제조사, kcal, 탄·단·지) → "가져오기"
   - 음식 직접 등록 (100g 기준): 이름, kcal, 탄수화물, 단백질, 지방

빈 상태: "아직 기록이 없어요. 위 칸에 먹은 걸 적어 보세요."

### 4-3. 주간 리포트 ⬜ — 새로 만들 화면
서버가 매일 밤 9시에 회원별 하루 요약과 추천을 저장해 둔다. 이걸 보여 주는 화면이 아직 없다.
- 기간 선택 (이번 주 / 지난주 / 직접 선택)
- 날짜별 칼로리 막대 + 목표선, 탄·단·지 비율
- 날짜별 그날 추천됐던 음식과 양
- 기록이 없는 날 표시
- 오늘 화면에서 들어올 수 있는 링크

### 4-4. 랜딩 (소개) 🟡
- 첫 화면: 서비스 이름 + 한 줄 소개 + "시작하기"
- "하루 식단은 세 가닥이에요": 매듭이 풀려 탄·단·지 세 고리로 나뉘는 3D 스크롤 연출 (three.js + GSAP, 현재 동작 중)
- 기능 3가지: 영양 자동 계산(계산 예시), 다음 끼니 추천(계산 근거), 매일 자동 리포트(서버 로그 예시)
- 마지막: "오늘 먹은 것부터 적어 보세요" + 시작하기 + 체험 계정 안내

## 5. 화면이 쓰는 API

| 기능 | 요청 | 주요 데이터 |
|---|---|---|
| 회원가입 | `POST /signup` (JSON) | `{ username, password }` |
| 로그인 | `POST /login` (폼 형식) | `username=...&password=...` |
| 로그아웃 | `POST /logout` | |
| 내 정보·목표 | `GET /me` | `username, targetKcal, targetCarbohydrate, targetProtein, targetFat` |
| 목표 저장 | `PUT /me/targets` | `{ kcal, carbohydrate, protein, fat }` |
| 하루 기록 | `GET /meals?date=YYYY-MM-DD` | `[{ id, mealType, grams, food: { id, name, kcalPer100g, carbohydrate, protein, fat, source } }]` |
| 기록 추가 | `POST /meals` | `{ date, mealType, foodId, grams }` |
| AI 기록 | `POST /meals/analyze` | `{ date, mealType, text }` → 저장된 기록 목록. 503 = AI 사용 불가, 422 = 음식 인식 실패 |
| 기록 삭제 | `DELETE /meals/{id}` | |
| 하루 요약 | `GET /summary?date=` | `totalKcal, totalCarbohydrate, totalProtein, totalFat, mealCount` |
| 다음 끼니 추천 | `GET /recommendation?date=` | `food, grams, kcal, nutrient, reason, neededGrams, capped, remainingKcal, shortfalls[{ nutrient, target, eaten, lack, ratio }]` (없으면 빈 응답) |
| 음식 목록 | `GET /foods` | `[{ id, name, kcalPer100g, ... }]` |
| 음식 등록 | `POST /foods` | `{ name, kcalPer100g, carbohydrate, protein, fat }` |
| 식약처 검색 | `GET /foods/public?keyword=` | `[{ name, maker, basis, kcal, carbohydrate, protein, fat }]` |
| 기간 리포트 | `GET /reports?from=&to=` | `[{ date, mealCount, totalKcal, totalCarbohydrate, totalProtein, totalFat, recommendedFood, recommendedGrams }]` |

- `mealType`: `BREAKFAST`(아침) · `LUNCH`(점심) · `DINNER`(저녁) · `SNACK`(간식)
- `food.source`: `SEED`(기본 음식) · `AI`(AI가 추정해 만든 음식)

## 6. 디자인 시안에 쓸 예시 데이터 (demo 계정)

- 목표: 2,000kcal / 탄수화물 300g / 단백질 60g / 지방 50g
- 오늘 기록: 아침 공기밥 150g(215kcal) / 점심 김치찌개 300g(150kcal, AI 추정), 공기밥 105g(150kcal)
- 합계: 515kcal, 탄수화물 88.1g, 단백질 19.9g, 지방 8.3g, 3끼
- 추천: 닭가슴살 174g, 190kcal, "단백질이 가장 부족해요" (목표 달성률 탄 29% · 단 33% · 지 17%)

---

## 7. 디자인 도구에 넣을 프롬프트

```
Design a mobile-first web app UI (Korean language) for "식단 자동화", a meal-logging service.
Users type what they ate in one sentence ("김치찌개 한 그릇이랑 밥 반 공기"); AI splits it into foods
and logs grams, calories, carbohydrate, protein and fat. The app then recommends the next meal that
fills the most-lacking nutrient, showing the calculation.

Brand: "a day of eating is three strands" — carbohydrate #d9a441, protein #2f6f4f, fat #b06a3b,
shown as three lines/bands. Surfaces are white frosted glass (translucent white, backdrop blur,
bright edge). Buttons are "liquid glass" pills: translucent white, soft inner shade at the top,
thin dark refraction line at the bottom edge, soft wide drop shadow, black text.
Background light #eef1ec, text #18201b, muted #5b655f. Font: Pretendard.
Avoid purple gradients and generic identical SaaS cards.

Screens:
1) Today dashboard (most important): date switcher; today's calories (large number) vs 2,000 kcal goal;
   three strand progress bars (carb 88.1/300g, protein 19.9/60g, fat 8.3/50g); a prominent
   "무엇을 먹었나요?" one-line AI input with meal chips (아침/점심/저녁/간식) and an "AI로 기록" button;
   meals grouped by meal time with per-group kcal and an "AI 추정" tag; a "다음 끼니 추천" panel
   ("닭가슴살 174g", 190kcal, reason, 3-step calculation, "이대로 기록" button) tinted by the lacking
   nutrient's strand color; collapsed sections for daily goals, public food search, and adding a food.
   Include empty, loading, and "AI unavailable" states.
2) Weekly report: daily calorie bars with a goal line, carb/protein/fat ratio per day, the food
   recommended each day, days without records.
3) Login/sign-up: white glass card with 3D tilt, ID/password fields, "체험 계정으로 들어가기".

Output must be implementable with React + TypeScript + Tailwind CSS v4 + shadcn/ui + framer-motion +
lucide-react (no Next.js-only APIs). Show mobile (390px) and desktop (1280px).
```
