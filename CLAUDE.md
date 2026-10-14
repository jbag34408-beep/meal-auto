# CLAUDE.md

## 응답 언어

- 이 프로젝트에서는 사용자에게 하는 모든 답변을 한국어로 작성한다.

## 화면 구조

- 백엔드: Spring Boot (src/main/java), 화면 파일은 src/main/resources/static 에서 서비스됨
- React로 옮긴 화면은 frontend/ (Vite + React + TypeScript + Tailwind v4 + shadcn 구조)
  - 공용 컴포넌트: frontend/src/components/ui (예: liquid-glass-button.tsx), 화면: frontend/src/pages/<화면>/
  - 화면을 새로 옮기면 frontend/vite.config.ts 의 build.rollupOptions.input 에 추가
  - `npm run build` → 결과가 src/main/resources/static 으로 나감 (login.html, assets/ 는 git에 올리지 않음, 배포 시 Dockerfile이 빌드)
- 아직 일반 HTML인 화면: static/index.html, static/landing.html (React로 옮기기 전까지 직접 수정)
- 로컬 확인: `./gradlew bootRun`(8080) + frontend에서 `npm run dev`(5173, API는 8080으로 프록시)
