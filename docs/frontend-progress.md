# 프론트엔드 작업 정리 (2026-10-15 기준)

> 10/14 로그인 화면 작업(`docs/frontend-login-progress.md`)에 이어, 10/15에 **식단 화면과 소개(랜딩) 화면**을 로그인과 같은 Aura 디자인의 React로 옮긴 내용.
> 다음 작업을 시작할 때 이 문서부터 본다.

---

## 1. 한눈에 보기

| 화면 | 주소 | 상태 |
|---|---|---|
| 소개(랜딩) | `/` → `/landing.html` | ✅ React + Aura, 스크롤 없는 한 화면 |
| 로그인 · 회원가입 | `/login.html` (`?mode=signup`이면 가입 카드부터) | ✅ React + Aura (10/14) |
| 오늘 식단 | `/index.html` | ✅ React + Aura (기존 기능 전부 이전) |

- **모든 화면이 React로 옮겨짐.** `src/main/resources/static`에 git으로 관리하는 화면 파일은 더 이상 없음 (전부 `npm run build` 결과물).
- 세 화면이 같은 디자인을 씀: 3D 액체 크롬 배경, 반투명 유리 카드, 알약 모양 입력·버튼, 라이트/다크 테마.
- 테마는 `localStorage["aura-theme"]` 하나로 공유 → 어느 화면에서 바꿔도 다른 화면도 같은 테마.
- 서비스 이름은 **아직 확정 전**. 화면에 "식단 자동화", `MEAL.AUTO` 문구가 남아 있음 (7번 참고).

## 2. 오늘 작업 흐름 (커밋 순서, 모두 `main`에 반영)

1. `2e6388f` 로그인 작업 정리 문서 추가 (`docs/frontend-login-progress.md`)
2. `ec9dbfd` **식단 화면을 React + Aura로 이전**
3. `538dbb7` **소개 화면을 React + Aura로 이전** (+ 21st.dev `motion-footer` 컴포넌트 도입, gsap 설치)
4. `e933e26` 소개 화면을 **스크롤 없는 한 화면**으로 정리, 배경을 3D 크롬으로 통일, `?mode=signup` 지원
5. `01e1759` 소개 화면 뒤의 큰 "MEAL AUTO" 글자 제거 (이름 미확정)

## 3. Figma 시안

- 파일: https://www.figma.com/design/pz20kDK90Uc03KJckG8tw8 ("식단 자동화 - Aura 디자인")
- 들어 있는 것: 로그인 기준 화면, 식단 화면 데스크톱 라이트/다크, 부품 모음(Components)
- 배경은 실제 3D 장면을 캡처한 이미지 (레이어 이름 `3D Chrome (capture)`)
- 주의
  - Figma 플랜이 **Starter + View 좌석** → Figma MCP 호출 한도가 낮음 (10/15에 한도 도달)
  - 변수 모드를 1개만 쓸 수 있어서 색 변수를 `light/…`, `dark/…`로 나눠 둠
  - Figma에는 Pretendard가 없어 한글은 Noto Sans KR로 들어가 있음 (실제 화면은 Pretendard)
  - 실제 화면과 다른 점: 시안의 "추천 3개"는 서버가 추천을 1개만 주므로 코드에서는 1개로 구현

## 4. 파일 구조 (frontend/)

```
frontend/
├── landing.html, login.html, index.html   # 화면별 진입 HTML (vite.config.ts의 input에 등록)
└── src/
    ├── lib/
    │   ├── api.ts            # 로그인·회원가입
    │   └── meal-api.ts       # 식단 화면 API·타입·날짜/숫자 도우미
    ├── components/
    │   ├── aura/             # ★ 세 화면 공용 디자인
    │   │   ├── aura.css              # 색 변수(라이트/다크), 유리 카드, 입력·버튼
    │   │   ├── aura-backdrop.tsx     # 3D 크롬 배경 (WebGL 없으면 그라데이션)
    │   │   ├── chrome-blob.tsx       # three.js 크롬 덩어리
    │   │   ├── use-aura-theme.ts     # 테마 저장·적용 (새로 분리)
    │   │   ├── theme-toggle.tsx      # LIGHT/DARK 알약 버튼 (새로 분리)
    │   │   ├── auth-card.tsx, brand-icons.tsx  # 로그인 카드
    │   └── ui/
    │       ├── motion-footer.tsx     # 소개 화면 (CinematicFooter, gsap)
    │       └── input.tsx, liquid-glass-button.tsx, sign-in-card.tsx  # 현재 미사용
    └── pages/
        ├── landing/  LandingPage.tsx, landing.css, main.tsx
        ├── login/    LoginPage.tsx, main.tsx
        └── meal/     MealPage.tsx, SummaryCard.tsx, LogCard.tsx, RecordsCard.tsx,
                      RecommendCard.tsx, ManageCard.tsx, parts.tsx, meal.css, main.tsx
```

## 5. 화면별 내용

### 소개 화면 (`pages/landing`)
- `CinematicFooter`를 `mode="page"`로 써서 **한 화면**만 보여 줌 (스크롤 없음, 좁거나 낮은 화면에서만 스크롤)
- 위쪽에 기울어진 띠로 기능 이름이 흐름 → 가운데 "오늘 먹은 것부터 적어 보세요"
- 버튼: **시작하기**(→ 로그인), **체험 계정으로 둘러보기**(demo로 바로 로그인 → 식단 화면)
- 보조 링크: 로그인, 회원가입(`/login.html?mode=signup`)
- 오른쪽 아래 테마 버튼, 처음 열릴 때 제목·버튼이 떠오르는 효과
- 이전 랜딩의 "세 가닥", "기능 3개" 섹션은 **삭제**함

### 식단 화면 (`pages/meal`)
- 위쪽 바(스크롤해도 고정): 서비스 이름, 날짜 이동(‹ 날짜 ›, 날짜 누르면 달력), 사용자 이름, 테마, 로그아웃
- 왼쪽 칸
  - **오늘 요약**: 큰 kcal 숫자(바뀌면 굴러가며 바뀌고 +/− 표시), 목표·남은 양·진행 바, 탄·단·지 타일
  - **무엇을 먹었나요?**: AI 문장 기록 + 끼니 칩, 접었다 펴는 "직접 입력하기"
    - AI가 503(키 없음·한도 초과)이면 직접 입력이 자동으로 펼쳐지고 음식 선택에 표시
  - **기록**: 끼니별 묶음, AI 추정 표시, 삭제(확인 창)
- 오른쪽 칸
  - **다음 끼니 추천**: 추천 음식·양·kcal, 부족한 영양소 막대, "추천량 계산 보기"(접힘), 끼니 칩 + "이대로 기록"
    - 지난 날짜면 제목이 "이날 더 먹었으면 좋았을 음식"
  - **목표와 음식 관리**: 하루 목표 저장 / 공공 데이터 검색·가져오기 / 내 음식 등록 (접었다 펴는 줄)
- 반응형: 1180px 아래 1단, 860px 아래 모바일 배치. 배경은 화면에 고정, 카드만 스크롤

### 로그인 화면 (`pages/login`)
- 10/14 내용 그대로. 테마 코드를 공용(`use-aura-theme`, `theme-toggle`)으로 바꾸고 `?mode=signup` 지원만 추가

## 6. 실행 · 빌드

```bash
# 백엔드 (Java 17 필요)
./gradlew bootRun              # Windows PowerShell: .\gradlew.bat bootRun

# 화면 빌드 (받은 뒤 한 번은 꼭 실행해야 화면이 뜸)
cd frontend
npm install                    # gsap 추가됨
npm run build                  # → src/main/resources/static 에 landing/login/index.html, assets/
```

- 개발 중: `bootRun` 켜 둔 채 `frontend`에서 `npm run dev` → http://localhost:5173/landing.html, /login.html, /index.html
- Windows PowerShell 5.x는 `&&`를 못 씀 → 한 줄씩 실행하거나 `;`로 연결
- 배포(Dockerfile)는 빌드를 알아서 함

## 7. 다음에 할 일 (후보)

1. **서비스 이름 확정 후 일괄 교체**
   - 소개: `© 2026 식단 자동화`, 띠 문구
   - 식단: 위쪽 바 "식단 자동화"와 `MEAL.AUTO` 태그
   - 로그인: 카드 태그 `AURA.IDENTITY`
   - 각 HTML의 `<title>`
   - 이름이 정해지면 소개 화면 큰 배경 글자도 다시 넣을 수 있음: `CinematicFooter`의 `giantText` 값만 주면 됨
2. **로그인 카드 한국어화**: Authenticate, IDENTIFIER, INITIALIZE UPLINK 등이 아직 영어. 가입 시 입력 조건 안내 추가
3. **로그인 소셜 버튼**: 서버 기능이 없음 → 버튼 제거 또는 안내 유지 결정
4. **비밀번호 보기 버튼** (로그인 카드)
5. **안 쓰는 코드 정리**
   - `components/ui/input.tsx`, `sign-in-card.tsx`, `liquid-glass-button.tsx`
   - `aura.css`의 `.aura-meta*`, `.aura-cursor`, `.aura-vertical`, `.aura-version`
   - `.gitignore`의 `docs/*.png` 중복 줄
6. **`docs/frontend-brief.md` 갱신**: 아직 이전 디자인(세 가닥 색, 하얀 유리) 기준. Aura(무채색) 기준으로 고치기
7. **용량**: 3D 배경 코드(`chrome-blob` 약 970KB)가 커서 빌드 경고가 나옴. 필요하면 three.js 쪽을 더 나누거나 가볍게

## 8. 참고 / 주의

- `src/main/resources/static/*.html`, `static/assets/`는 빌드 결과물이라 git에 없음 → 받은 뒤 `npm run build` 필수
- 화면은 모두 같은 주소에서 API를 부르고 세션 쿠키로 로그인 유지 (401이면 로그인 화면으로 이동)
- 3D 배경은 WebGL이 없으면 CSS 그라데이션만 보이고, "동작 줄이기" 설정이면 움직임·흐르는 띠·버튼 끌림이 꺼짐
