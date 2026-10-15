# 프론트엔드 로그인 영역 작업 정리 (2026-10-14 기준)

> 어제(10/14) 진행한 로그인 화면 React 전환 작업을 정리한 문서. 오늘 이어서 작업할 때 출발점으로 쓴다.

---

## 1. 한눈에 보기

| 항목 | 상태 |
|---|---|
| React 프론트엔드 도입 (Vite + React 19 + TS + Tailwind v4 + shadcn 구조) | ✅ 완료 |
| 로그인 화면을 React로 이전 (`frontend/login.html`) | ✅ 완료 |
| 최종 디자인: **Aura** (3D 액체 크롬 배경 + 유리 카드 + 라이트/다크 테마) | ✅ 적용됨 |
| 로그인 / 회원가입 / 체험 계정 서버 연동 | ✅ 동작 |
| 소셜 로그인 (Apple / Google / GitHub) | ⬜ 버튼만 있음 (누르면 "coming soon" 안내) |
| 카드 문구 한국어화 | ⬜ 아직 영어 (Aura 원본 문구 그대로) |
| 안 쓰는 이전 디자인 코드·CSS 정리 | ⬜ 남아 있음 |
| `docs/frontend-brief.md` 로그인 항목 갱신 | ⬜ 이전 디자인(sign-in-card) 설명 그대로 |

## 2. 어제 작업 흐름 (커밋 순서)

디자인을 세 번 바꿨고, 마지막 Aura 디자인이 현재 화면이다.

1. `7909b90` **React 프론트엔드 도입** — 로그인 화면을 React + Liquid Glass 버튼으로 전환
   - `frontend/` 생성, 빌드 결과를 `src/main/resources/static` 으로 출력 (git 제외)
   - Dockerfile에 Node 빌드 단계 추가, 기존 `static/login.html` 삭제
   - `SecurityConfig`에서 `/assets/**` 로그인 없이 허용
2. `17044ef` **sign-in-card-2 기반 하얀 유리 디자인** — 3D 기울임 카드, 테두리 빛줄기, framer-motion 도입
3. `884b315` 디자인 브리프 문서 추가 (`docs/frontend-brief.md`)
4. `97837bc` **액체 크롬 배경 + 유리 카드 (Aura)** — three.js 크롬 덩어리 배경, 라이트/다크 테마
5. `30c16ab` 모서리 장식 글씨 제거 (`SYS.CORE // ON-LINE`, `UPLINK_ESTABLISHED`, 세로 글씨, `V03.01`)
6. `fb4e8e6` 마무리 커밋 (실제 변경은 `.gitignore` 한 줄)

## 3. 현재 파일 구조

```
frontend/
├── login.html                         # 진입 HTML (폰트: Pretendard, Space Grotesk, JetBrains Mono)
├── vite.config.ts                     # build.rollupOptions.input 에 login 등록, dev 프록시 설정
└── src/
    ├── index.css                      # Tailwind + 앱 색상 토큰(@theme) + .liquid-glass
    ├── lib/
    │   ├── api.ts                     # login(), signup() — Spring Boot 호출
    │   └── utils.ts                   # cn() 클래스 합치기
    ├── pages/login/
    │   ├── main.tsx                   # React 진입점
    │   └── LoginPage.tsx              # ★ 화면 조립 + 테마 + 서버 요청
    └── components/
        ├── aura/                      # ★ 현재 쓰는 디자인
        │   ├── aura.css               # 화면 전체 스타일 (라이트/다크, 반응형)
        │   ├── aura-backdrop.tsx      # 배경: CSS 그라데이션 → 준비되면 3D 크롬
        │   ├── chrome-blob.tsx        # three.js 액체 크롬 덩어리 (지연 로딩)
        │   ├── auth-card.tsx          # 로그인/가입 유리 카드
        │   └── brand-icons.tsx        # Apple/Google/GitHub 단색 로고
        └── ui/
            ├── input.tsx              # shadcn Input        (현재 미사용)
            ├── liquid-glass-button.tsx# Liquid Glass 버튼   (현재 미사용)
            └── sign-in-card.tsx       # 이전 디자인 카드     (현재 미사용)
```

## 4. 주요 동작

### LoginPage.tsx
- 상태: `theme`(light/dark), `mode`(login/signup), `busy`, `error`
- 테마는 `localStorage["aura-theme"]` 에 저장, `<html>` 배경색도 같이 바꿈
- 로그인/가입 성공 시 `location.href = "/index.html"` (오늘 식단 화면)
- 회원가입은 `signup()` 후 바로 `login()` 까지 이어서 실행
- 체험 계정: `demo / demo1234` 로 바로 로그인

### AuthCard (`components/aura/auth-card.tsx`)
- 같은 카드 안에서 로그인 ↔ 회원가입 전환
- 입력은 `FormData` 로 읽음 (비제어 입력), 아이디는 `trim()`
- 오류는 `role="alert"` 문단에 표시, 처리 중엔 입력·버튼 비활성 + 스피너
- 소셜 버튼: 서버 기능이 없어 2.6초간 안내 문구만 표시

### 배경 (`aura-backdrop.tsx`, `chrome-blob.tsx`)
- three.js 코드는 `lazy()` 로 따로 받아 첫 화면 로딩을 막지 않음
- WebGL이 없거나 3D가 오류 나면 CSS 그라데이션 배경만 남음 (`SceneBoundary`)
- simplex noise로 구 표면을 굽이치게 만들고, 테마별 금속 색·환경 지도를 직접 칠함
- 마우스 패럴랙스, 탭이 숨겨지면 렌더링 정지, "동작 줄이기" 설정이면 움직임 멈춤
- 화면 비율에 따라 덩어리 위치 조정 (가로: 왼쪽~가운데, 세로/모바일: 카드 뒤 위쪽)

### 서버 연동 (`lib/api.ts`)
- `POST /login` — Spring Security 폼 로그인 (`username=...&password=...`)
- `POST /signup` — JSON, 실패 시 서버의 `errors`/`message` 를 그대로 보여 줌
- 세션 쿠키 방식 (같은 주소라서 자동으로 붙음)

## 5. 실행 방법

```bash
# 1) 백엔드 (8080)
./gradlew bootRun

# 2) 프론트 개발 서버 (5173, API는 8080으로 프록시)
cd frontend
npm install        # 처음 한 번
npm run dev        # http://localhost:5173/login.html

# 배포용 빌드 → src/main/resources/static/login.html, assets/ 생성 (git 제외)
npm run build
```

## 6. 오늘 이어서 할 일 (후보)

우선순위 순으로 정리. 필요한 것부터 골라서 진행.

1. **문구 한국어화** — `auth-card.tsx` 의 `COPY`, 라벨(IDENTIFIER/PASSCODE), 버튼(INITIALIZE UPLINK), 안내 문구가 영어.
   서비스 대상이 한국 사용자이므로 한국어로 바꾸고, 가입 모드엔 입력 조건 안내 추가
   (이전 디자인 기준: 아이디 영문·숫자 4~20자, 비밀번호 8자 이상)
2. **소셜 로그인 버튼 처리** — 서버에 기능이 없으므로 (a) 버튼 제거 또는 (b) 안내 유지 중 결정
3. **비밀번호 보기 버튼** — 이전 디자인엔 있었는데 Aura 카드엔 없음
4. **안 쓰는 코드 정리**
   - `components/ui/sign-in-card.tsx`, `input.tsx` (이전 디자인)
   - `aura.css` 의 `.aura-meta*`, `.aura-cursor`, `.aura-vertical`, `.aura-version` (30c16ab에서 화면에서 뺐지만 CSS는 남음)
   - `.gitignore` 의 `docs/*.png` 중복 줄
   - `liquid-glass-button.tsx` 는 다른 화면에서 쓸 수 있으니 남겨 둘지 결정
5. **브랜드 일관성 확인** — 카드 태그가 `AURA.IDENTITY`. 브리프의 "세 가닥"(탄·단·지 색) 표식과 서비스 이름(식단 자동화)이 로그인 화면에 없음
6. **`landing.html` 로 가는 링크** — 이전 디자인엔 "식단 자동화는 어떻게 동작하나요?" 링크가 있었는데 지금은 없음
7. **`docs/frontend-brief.md` 4-1 항목 갱신** — 현재 Aura 디자인 기준으로
8. **다음 화면 React 이전** — `static/index.html`(식단 화면), `static/landing.html`.
   옮기면 `vite.config.ts` 의 `rollupOptions.input` 에 추가

## 7. 참고 / 주의

- `src/main/resources/static/login.html`, `static/assets/` 는 빌드 결과물이라 git에 없음 → 배포 시 Dockerfile이 빌드
- `vite.config.ts` 의 `emptyOutDir: false` 는 아직 React로 안 옮긴 `index.html`, `landing.html` 을 지우지 않기 위한 것
- 디자인 기준 이미지 `docs/*.png` 는 로컬에만 있음 (git 제외)
- `SecurityConfig` 에서 로그인 없이 접근 가능한 경로: `/`, `/landing.html`, `/login.html`, `/assets/**`, `/signup`, `/login`
