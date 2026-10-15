# CLAUDE.md

## 응답 언어

- 이 프로젝트에서는 사용자에게 하는 모든 답변을 한국어로 작성한다.

## 화면 구조

- 백엔드: Spring Boot (src/main/java), 화면 파일은 src/main/resources/static 에서 서비스됨
- React로 옮긴 화면은 frontend/ (Vite + React + TypeScript + Tailwind v4 + shadcn 구조)
  - 공용 컴포넌트: frontend/src/components/ui (예: liquid-glass-button.tsx), 화면: frontend/src/pages/<화면>/
  - 화면을 새로 옮기면 frontend/vite.config.ts 의 build.rollupOptions.input 에 추가
  - 옮긴 화면: 소개(frontend/landing.html → pages/landing), 로그인(frontend/login.html → pages/login), 오늘 식단(frontend/index.html → pages/meal) — 모든 화면이 React
  - 공용 Aura 디자인: frontend/src/components/aura (3D 배경, 테마), 소개 화면 푸터: components/ui/motion-footer.tsx (gsap)
  - `npm run build` → 결과가 src/main/resources/static 으로 나감 (landing.html, login.html, index.html, assets/ 는 git에 올리지 않음, 배포 시 Dockerfile이 빌드)
- 로컬 확인: `./gradlew bootRun`(8080) + frontend에서 `npm run dev`(5173, API는 8080으로 프록시)
