---
name: 식단 자동화 (meal-auto)
description: 먹은 음식을 기록하면 영양을 계산하고 다음 끼니를 추천해요.
colors:
  # 두 세계가 공유하는 다리: 세 영양소 색 (landing.html, index.html에서 같은 값)
  carb: "#d9a441"
  protein: "#2f6f4f"
  fat: "#b06a3b"
  # 랜딩 세계 (어두운 바닥, landing.html)
  landing-ground: "#0f1311"
  landing-floor: "#161c19"
  landing-ink: "#ecefe9"
  landing-muted: "#a3aca6"
  landing-line: "rgba(236, 239, 233, 0.14)"
  carb-ink: "#e2b25a"
  protein-ink: "#7fbf9c"
  fat-ink: "#d98e5f"
  # 앱 세계 (밝은 바닥, index.html · login.html, 기존 운영 화면)
  app-ground: "#f6f4ef"
  app-card: "#ffffff"
  app-text: "#1f2421"
  app-muted: "#6b716d"
  app-line: "#e4e0d8"
  app-accent: "#2f6f4f"
  app-accent-soft: "#e3efe8"
  app-rec-muted: "#4d6457"
  app-rec-line: "#c9dccf"
  app-danger: "#b4443a"
typography:
  landing-display:
    fontFamily: "Pretendard Variable, Pretendard, Apple SD Gothic Neo, Malgun Gothic, sans-serif"
    fontSize: "clamp(56px, 10.5vw, 148px)"
    fontWeight: 800
    lineHeight: 0.98
    letterSpacing: "-0.04em"
  landing-headline:
    fontFamily: "Pretendard Variable, Pretendard, Apple SD Gothic Neo, Malgun Gothic, sans-serif"
    fontSize: "clamp(32px, 4.6vw, 60px)"
    fontWeight: 750
    lineHeight: 1.12
    letterSpacing: "-0.03em"
  landing-figure:
    fontFamily: "Pretendard Variable, Pretendard, Apple SD Gothic Neo, Malgun Gothic, sans-serif"
    fontSize: "clamp(36px, 4.4vw, 56px)"
    fontWeight: 800
    lineHeight: 1.05
    letterSpacing: "-0.035em"
    fontFeature: "tnum"
  landing-title:
    fontFamily: "Pretendard Variable, Pretendard, Apple SD Gothic Neo, Malgun Gothic, sans-serif"
    fontSize: "clamp(22px, 2vw, 27px)"
    fontWeight: 700
    letterSpacing: "-0.02em"
  landing-body:
    fontFamily: "Pretendard Variable, Pretendard, Apple SD Gothic Neo, Malgun Gothic, sans-serif"
    fontSize: "17px"
    fontWeight: 400
    lineHeight: 1.6
  landing-caption:
    fontFamily: "Pretendard Variable, Pretendard, Apple SD Gothic Neo, Malgun Gothic, sans-serif"
    fontSize: "14px"
    fontWeight: 400
  landing-mono:
    fontFamily: "ui-monospace, Cascadia Mono, Consolas, D2Coding, monospace"
    fontSize: "14px"
    lineHeight: 1.75
  app-title:
    fontFamily: "Pretendard, Apple SD Gothic Neo, Malgun Gothic, sans-serif"
    fontSize: "26px"
    fontWeight: 700
    letterSpacing: "-0.5px"
  app-figure:
    fontFamily: "Pretendard, Apple SD Gothic Neo, Malgun Gothic, sans-serif"
    fontSize: "40px"
    fontWeight: 700
    letterSpacing: "-1px"
    fontFeature: "tnum"
  app-heading:
    fontFamily: "Pretendard, Apple SD Gothic Neo, Malgun Gothic, sans-serif"
    fontSize: "16px"
    fontWeight: 600
  app-body:
    fontFamily: "Pretendard, Apple SD Gothic Neo, Malgun Gothic, sans-serif"
    fontSize: "15px"
    fontWeight: 400
  app-label:
    fontFamily: "Pretendard, Apple SD Gothic Neo, Malgun Gothic, sans-serif"
    fontSize: "12px"
    fontWeight: 400
rounded:
  pill: "999px"
  landing-bar: "3px"
  app-bar: "4px"
  app-row: "6px"
  app-control: "8px"
  app-card: "14px"
spacing:
  landing-gutter: "clamp(20px, 5vw, 72px)"
  landing-feature-max: "1320px"
  app-container: "760px"
  app-page-x: "16px"
  app-card-pad: "20px"
  app-card-gap: "16px"
  app-form-gap: "8px"
components:
  landing-button-primary:
    backgroundColor: "{colors.landing-ink}"
    textColor: "{colors.landing-ground}"
    rounded: "{rounded.pill}"
    padding: "0 34px"
    height: "56px"
  landing-button-primary-hover:
    backgroundColor: "#ffffff"
    textColor: "{colors.landing-ground}"
    rounded: "{rounded.pill}"
  app-button-primary:
    backgroundColor: "{colors.app-accent}"
    textColor: "{colors.app-card}"
    rounded: "{rounded.app-control}"
    padding: "9px 12px"
  app-button-ghost:
    backgroundColor: "transparent"
    textColor: "{colors.app-muted}"
    rounded: "{rounded.app-control}"
    padding: "6px 10px"
  app-input:
    backgroundColor: "{colors.app-card}"
    textColor: "{colors.app-text}"
    rounded: "{rounded.app-control}"
    padding: "9px 12px"
  app-card:
    backgroundColor: "{colors.app-card}"
    textColor: "{colors.app-text}"
    rounded: "{rounded.app-card}"
    padding: "20px"
  app-card-recommend:
    backgroundColor: "{colors.app-accent-soft}"
    textColor: "{colors.app-text}"
    rounded: "{rounded.app-card}"
    padding: "20px"
  app-delta-chip:
    backgroundColor: "{colors.app-accent-soft}"
    textColor: "{colors.app-accent}"
    rounded: "{rounded.pill}"
    padding: "2px 9px"
---

# Design System: 식단 자동화 (meal-auto)

## Overview

**Creative North Star: "세 가닥, 두 바닥"**

이 프로젝트에는 일부러 서로 다른 바닥을 가진 두 시각 세계가 함께 있어요. **랜딩 세계**(`landing.html`)는 초록빛이 도는 거의 검은 바닥 위에서 하루 식단을 탄수화물·단백질·지방 세 가닥이 꼬인 하나의 매듭으로 보여 주고, 스크롤에 따라 매듭을 풀었다가 마지막에 다시 묶어요. 카드가 없고 머리카락처럼 얇은 선과 큰 글자, 버튼 하나로만 이루어진 설득용 무대예요. **앱 세계**(`index.html`, `login.html`)는 따뜻한 미색 바닥 위 흰 카드들로 이루어진 기존 운영 화면이에요. 기록하고, 다시 계산되는 걸 확인하고, 추천을 받는 밀도 높은 작업 화면이에요.

두 세계를 잇는 다리는 두 가지예요. 하나는 **세 영양소 색**(`carb`, `protein`, `fat`)으로, 두 파일에서 같은 값으로 선언되고 같은 의미로만 쓰여요. 다른 하나는 **~해요 말투**예요. 그 밖의 바닥색, 표면, 버튼 모양, 글자 크기는 의도적으로 달라요. 랜딩은 어둡고 연극적이며, 앱은 밝고 조용해요. 새 화면은 먼저 어느 세계에 속하는지 정한 뒤 그 세계의 규칙만 따라요.

앱 세계는 이 문서에서 **있는 그대로 기록**한 것이며 변경 제안이 아니에요. 앱의 미색 바닥(`app-ground`)은 감지 도구가 cream-palette로 표시했고, 이 바닥을 유지할지는 **사용자가 아직 결정하지 않은 열린 문제**예요. 이 문서는 그 결정을 내리지 않아요.

**Key Characteristics:**
- 두 바닥: 랜딩은 어두운 초록빛 검정(`landing-ground`), 앱은 따뜻한 미색(`app-ground`). 서로 섞지 않아요.
- 세 영양소 색은 두 세계 공통의 유일한 색 언어예요.
- 랜딩: 카드 없음, 얇은 선만, 알약형 버튼 하나, Three.js 세 가닥 매듭과 GSAP 스크롤 문법.
- 앱: 흰 카드, 숲 초록 강조색 하나, 8px 모서리 컨트롤, 재계산을 보여 주는 작은 움직임.
- 숫자는 두 세계 모두 `tabular-nums`로 자릿수를 맞춰요.
- 한국어 UI, `lang="ko"`, `word-break: keep-all`(랜딩 전체, 앱 추천 카드).

## Colors

세 영양소 색이 두 세계를 잇고, 그 외의 색은 각 세계의 바닥에 맞춰 따로 존재해요.

### Primary
- **곡물 금빛 (Grain Gold)** (`carb`): 탄수화물. 랜딩에서는 매듭 가닥, 왼쪽 스포트라이트, 비율 막대, 밑줄에 쓰이고, 앱에서는 탄수화물 진행 막대에 쓰여요. 랜딩의 `::selection` 배경이기도 해요.
- **숲 단백질 초록 (Forest Protein)** (`protein`): 단백질. 랜딩에서는 가닥·스포트라이트·막대에, 앱에서는 단백질 막대에 쓰여요. 앱의 강조색(`app-accent`)과 같은 값이에요. 어두운 바닥에서 글자로 쓰면 읽히지 않으므로 랜딩 글자에는 쓰지 않아요.
- **구운 아몬드 갈색 (Roasted Almond)** (`fat`): 지방. 랜딩에서는 가닥, 뒤에서 비추는 테두리 조명, 막대에, 앱에서는 지방 막대에 쓰여요.

### Secondary (랜딩 전용 글자용 영양소 색)
- **밝은 금빛 / 밝은 초록 / 밝은 갈색** (`carb-ink`, `protein-ink`, `fat-ink`): 어두운 바닥 위에서 영양소 이름과 수치를 글자로 쓸 때만 써요(가닥 캡션 제목, 매크로 합계 줄, 포커스 링은 `carb-ink`). 면·선·3D에는 원래 영양소 색을 써요.

### Neutral: 랜딩 세계
- **이끼 낀 밤 (Moss Night)** (`landing-ground`): 페이지 배경이자 Three.js 장면 배경과 안개 색. CSS와 WebGL이 같은 값을 써서 캔버스 경계가 보이지 않아요.
- **젖은 바닥 (Wet Floor)** (`landing-floor`): 3D 바닥면 재질 색. 세 조명의 색 그림자가 섞이는 곳이에요. CSS에서는 쓰지 않아요.
- **서리 잉크 (Frost Ink)** (`landing-ink`): 제목과 강조 수치, 버튼 면.
- **이끼 회색 (Moss Grey)** (`landing-muted`): 본문 보조 문장, 캡션, 스크롤 안내, 로그 시각.
- **머리카락 선 (Hairline)** (`landing-line`): 합계 위 구분선, 리포트 구역 위 선, 로그 위 선. 유일한 구획 수단이에요.

### Neutral: 앱 세계 (기존 운영 화면, 기록만 함)
- **따뜻한 미색 (Warm Paper)** (`app-ground`): 앱과 로그인 화면 배경. 감지 도구가 cream-palette로 표시했고, 유지 여부는 사용자 결정 대기 중이에요.
- **흰 카드 (Card White)** (`app-card`): 카드, 입력칸 배경, 주 버튼 글자.
- **먹색 (Ink Green-Black)** (`app-text`): 본문과 제목.
- **회녹색 (Sage Grey)** (`app-muted`): 카드 소제목, 표 머리, 보조 문장, ghost 버튼 글자.
- **모래 선 (Sand Line)** (`app-line`): 카드 테두리, 표 줄, 입력 테두리, 진행 막대 바탕.
- **숲 초록 (Forest Accent)** (`app-accent`): 주 버튼, 활성 탭 밑줄, 안내 문구, 증감 칩 글자.
- **연초록 (Mint Wash)** (`app-accent-soft`): 추천 카드 바탕, 증가 칩 바탕.
- **추천 회녹 / 추천 선** (`app-rec-muted`, `app-rec-line`): 연초록 추천 카드 안에서만 쓰는 보조 글자와 구분선.
- **벽돌 빨강 (Brick)** (`app-danger`): 오류 문구와 삭제 버튼 글자.

### Named Rules
**세 가닥 색 규칙 (The Three Strands Rule).** `carb`, `protein`, `fat`은 언제나 각각 탄수화물·단백질·지방만 뜻해요. 장식이나 일반 강조에 빌려 쓰지 않고, 두 세계에서 값을 바꾸지 않아요.

**어두운 바닥 글자 규칙 (The Ink Tint Rule).** 랜딩의 어두운 바닥에서 영양소를 글자로 쓸 때는 `-ink` 색을 써요. 원래 영양소 색은 면, 선, 빛, 3D 재질에만 써요.

**두 바닥 규칙 (The Two Grounds Rule).** 랜딩 바닥과 앱 바닥은 서로의 화면에 들어가지 않아요. 앱 화면에 어두운 무대를, 랜딩에 흰 카드를 가져오지 않아요.

## Typography

**Display Font:** Pretendard Variable (랜딩에서 CDN 동적 서브셋으로 실제 로드, Apple SD Gothic Neo · Malgun Gothic 폴백)
**Body Font:** 랜딩은 같은 Pretendard Variable. 앱은 `"Pretendard", "Apple SD Gothic Neo", "Malgun Gothic"` 스택을 선언하지만 웹폰트를 로드하지 않아서, 대부분의 Windows 환경에서는 Malgun Gothic으로 보여요(현재 상태 기록).
**Label/Mono Font:** `ui-monospace, Cascadia Mono, Consolas, D2Coding` (랜딩의 서버 로그 예시 전용)

**Character:** 랜딩은 아주 무겁고 촘촘하게 좁힌 한글 제목과 담백한 본문의 대비로 말하고, 앱은 작고 고른 크기로 데이터가 먼저 보이게 해요.

### Hierarchy (랜딩)
- **Display** (800, `clamp(56px, 10.5vw, 148px)`, 0.98, -0.04em): 첫 화면의 "식단 자동화" 한 번만.
- **Headline** (750, `clamp(32px, 4.6vw, 60px)`, 1.12, -0.03em): 각 구간 제목.
- **Figure** (800, `clamp(36px, 4.4vw, 56px)`, -0.035em): 계산 결과 하나("아몬드 94g"). 합계 줄은 800, `clamp(24px, 2.6vw, 32px)`.
- **Title** (700, `clamp(22px, 2vw, 27px)`, -0.02em): 기능 이름. 가닥 캡션 제목은 700, 22px(모바일 18px), 영양소 `-ink` 색과 2px 영양소 색 밑줄.
- **Body** (400, 17px / 모바일 16px, 1.6): 보조 문장은 `landing-muted`, 24–36ch로 짧게 끊어요.
- **Caption** (400, 14px, `landing-muted`): 예시 데이터의 출처를 밝히는 문장("demo 계정의 하루 기록 예시")과 스크롤 안내.
- **Mono** (14px / 1.75, 모바일 13px): 서버 로그 예시. 시각은 `landing-muted`.

### Hierarchy (앱)
- **Title** (700, 26px, -0.5px): 페이지 제목 "오늘의 식단". 로그인 카드 제목은 24px.
- **Figure** (700, 40px, -1px, tabular): 하루 kcal. 단위는 16px 500 `app-muted`.
- **Heading** (600, 16px, `app-muted`): 카드 소제목.
- **Body** (400, 15px 표 / 14px 보조): 표와 막대 라벨.
- **Label** (400, 12px, `app-muted`): 목표 입력칸 위 라벨.

### Named Rules
**한 번의 거대함 규칙 (The One Giant Rule).** 랜딩에서 Display 크기는 첫 화면 제목 하나에만 써요. 그다음으로 큰 글자는 실제로 계산된 숫자예요.

**맞춘 자릿수 규칙 (The Tabular Rule).** kcal, g, % 같은 수치는 두 세계 모두 `font-variant-numeric: tabular-nums`로 써서 계산이 정확해 보이게 해요.

## Layout

**랜딩.** 화면 전체를 덮는 고정 캔버스(`#stage`, z-index 0) 위로 글이 흘러가요. 좌우 여백은 `landing-gutter`. 첫 화면은 제목을 왼쪽 위에 크게, 매듭은 가운데, 스크롤 안내는 아래 가운데에 두고, 첫 화면에는 내비게이션과 버튼이 없어요. 풀림 구간은 화면에 고정(pin)되고 각 가닥 캡션은 3D 고리 위치를 화면 좌표로 투영해 따라붙어요. 가로 화면(종횡비 0.8 이상)은 세 고리를 가로로 늘어놓고 캡션을 아래 가운데에, 세로 화면은 왼쪽에 세로로 쌓고 캡션을 오른쪽에 둬요. 기능 구간은 최대 1320px의 12열 그리드에서 일부러 엇갈리게 배치해요(계산 1–7열, 추천 6–12열, 리포트는 위에 선을 긋고 4열 글 + 8열 로그). 마지막 구간은 화면 높이를 다 쓰고 내용을 바닥에 붙여요. 760px 이하에서는 모든 기능이 한 열로 쌓여요. 구간 사이 간격은 `clamp()`와 vh로 크게 열어 둬요.

**앱.** 최대 760px 가운데 정렬 한 열, 좌우 16px. 카드가 16px 간격으로 세로로 쌓이고 카드 안 여백은 20px. 폼은 8px 간격 그리드이며, 560px 이하에서 2열로 접혀요. 로그인은 화면 가운데 최대 380px 카드 하나예요.

## Elevation & Depth

두 세계 모두 CSS `box-shadow`를 쓰지 않아요. 깊이를 만드는 방식이 세계마다 달라요.

**랜딩: 빛과 그림자로 만든 실제 3D.** 깊이는 오직 Three.js 장면에서 나와요. 세 가닥은 크롬과 유리 사이의 클리어코트 금속(`MeshPhysicalMaterial`: metalness 0.45, roughness 0.3, clearcoat 1, clearcoatRoughness 0.08, envMapIntensity 0.95)이고, 낮은 강도의 `RoomEnvironment` 반사를 받아요. 세 영양소 색 스포트라이트(탄수화물 2.6 왼쪽 위, 단백질 7.5 오른쪽 위로 어두운 초록이라 더 세게, 지방 2.8 뒤쪽 테두리 빛)가 각각 그림자를 드리워서 바닥에 색이 섞인 그림자가 생겨요. 약한 반구광(0.35)이 바닥을 받치고, 바닥 색 안개(10–20)가 먼 곳을 바닥색으로 녹여요. 톤 매핑은 ACES Filmic, 그림자는 PCF Soft(데스크톱 1024, 모바일 512).

**앱: 평평한 층.** 미색 바닥 위 흰 카드와 1px 모래 선 테두리만으로 층을 나눠요. 추천 카드는 테두리 없이 연초록 면으로 구분해요.

### Named Rules
**빛으로만 깊이 규칙 (The Light-Only Depth Rule).** 랜딩의 깊이는 3D 조명과 그림자에서만 나와요. HTML 요소에 그림자, 블러, 유리 효과를 더하지 않아요.

**평평한 카드 규칙 (The Flat Card Rule).** 앱 카드는 그림자 없이 테두리와 면 색으로만 구분돼요.

## Shapes

**랜딩.** 모서리가 거의 없는 세계예요. 둥근 형태는 단 두 가지예요. 하나뿐인 알약형 버튼(`pill`)과 추천 근거의 가는 비율 막대(높이 6px, `landing-bar`). 나머지 구획은 모두 1px 머리카락 선이에요. 대표 형태는 (2,3) 토러스 매듭이에요. 세 관이 하나의 중심선을 공유하며 여섯 번 꼬이고, 열리면 세 개의 고리가 돼요. WebGL이 없을 때는 같은 세 색의 10px 테두리 원 세 개가 겹친 정지 그림이 이 형태를 대신해요.

**앱.** 부드러운 작은 모서리 체계예요. 컨트롤은 8px, 카드는 14px, 진행 막대는 4px, 추천 근거 줄은 6px, 증감 칩은 알약형이에요.

## Components

### Buttons
- **랜딩 주 버튼 (하나뿐):** 알약형(`pill`), 높이 56px, 좌우 34px, `landing-ink` 면에 `landing-ground` 글자, 18px 700. 페이지 맨 끝 "시작하기"에만 있고 `/login.html`로 가요.
- **Hover / Active:** 면이 순백(#ffffff)으로 밝아지고 2px 떠올라요(200ms `--ease-out`). 누르면 0.98로 줄어요. 포커스는 `carb-ink` 2px 외곽선, 4px 간격. 움직임 줄이기 설정에서는 전환이 꺼져요.
- **앱 주 버튼:** `app-accent` 면, 흰 글자, 8px 모서리, 9px 12px 여백. hover는 불투명도 0.9, 누르면 0.97로 줄고(120ms), 비활성은 0.5.
- **앱 ghost 버튼:** 투명 면, `app-muted` 글자, 1px `app-line` 테두리, 6px 10px, 13px(로그아웃). 삭제 버튼은 배경 없이 `app-danger` 글자.

### Chips
- **앱 증감 칩:** 하루 kcal 옆에 잠깐 떠오르는 알약형 칩. 증가는 `app-accent-soft` 바탕에 `app-accent` 글자, 감소는 `app-line` 바탕에 `app-rec-muted` 글자. 2.4초 동안 떠올랐다 사라져요. 랜딩에는 칩이 없어요.

### Cards / Containers
- **랜딩:** 카드가 없어요. 내용은 머리카락 선과 여백으로만 묶여요.
- **앱 카드:** 14px 모서리, `app-card` 면, 1px `app-line` 테두리, 그림자 없음, 안 여백 20px.
- **앱 추천 카드:** 같은 모양에 `app-accent-soft` 면, 테두리 투명, 안쪽 보조 글자와 선은 `app-rec-muted`, `app-rec-line`.

### Inputs / Fields
- **앱:** 흰 면, 1px `app-line` 테두리, 8px 모서리, 9px 12px(로그인은 11px 12px). 포커스는 브라우저 기본 외곽선을 그대로 써요. 오류는 입력 아래 14px `app-danger` 문구, 성공·안내는 `app-accent` 문구.
- **랜딩:** 입력 요소가 없어요.

### Navigation
- **랜딩:** 내비게이션이 없어요. 스크롤 자체가 길잡이이고, 첫 화면 아래 "스크롤해 보세요"와 위에서 아래로 자라는 1px 선(2.4초 반복)이 유일한 안내예요.
- **앱:** 위쪽 머리글에 제목과 사용자 이름, 로그아웃, 날짜 입력. 로그인 화면은 두 탭(로그인/회원가입)이고 활성 탭은 `app-text` 600 글자와 2px `app-accent` 밑줄이에요.

### 세 가닥 매듭 무대 (랜딩 대표 컴포넌트)
- **장면:** 고정 캔버스, 카메라 시야각 32°, (0, 1.2, 9)에서 약간 아래를 봐요. 픽셀 비율 상한은 데스크톱 1.75, 모바일 1.5. 관 단면 분할은 데스크톱 360×12, 모바일 240×10.
- **스크롤 문법 (GSAP ScrollTrigger):** ① 로드 직후 느슨한 매듭이 2.4초 `expo.out`으로 조여지고, 가닥이 멈춰 있는 동안 매듭은 천천히 자전해요. ② 첫 화면 15%까지 스크롤하면 스크롤 안내가 사라져요. ③ 풀림 구간을 230% 길이로 고정하고(scrub 0.8) 매듭 → 큰 고리 → 세 개의 작은 고리로 풀어요. 자전은 가장 가까운 정면 각도로 돌아와 멈추고, 각 가닥 캡션은 0.76 / 0.83 / 0.90 지점에서 차례로 나타나요. ④ 기능 구간으로 들어서면 캔버스가 사라지고(70%→15%), 화면 밖에서는 렌더링을 멈춰요. ⑤ 마지막 구간에서 캔버스가 다시 나타나고 세 가닥이 다시 매듭으로 묶이며 버튼 옆으로 비켜나요(가로 화면은 오른쪽 위, 세로 화면은 위).
- **움직임 줄이기 / GSAP 없음:** 회전과 전환 없이 구간마다 정지 장면 한 장으로 바뀌어요. 첫 화면은 매듭, 풀림 구간은 나뉜 세 고리, 기능 구간은 3D 없음, 마지막은 다시 매듭. 어떤 구간이 가장 많이 보이는지에 따라 교체돼요.
- **WebGL 없음:** 캔버스를 숨기고 CSS로 그린 세 색 원 세 개를 첫 화면에 남겨요. 글과 버튼만으로 페이지가 완결돼요.
- **탭이 숨겨지면** 렌더링 루프를 멈춰요.

### 근거 데이터 블록 (랜딩)
- **계산 표:** 음식·환산식·kcal 세 열, 합계 줄은 위에 머리카락 선을 긋고 크게. 아래 매크로 합계는 각 `-ink` 색 600.
- **추천 사슬:** 영양소 이름·6px 영양소 색 막대(폭 = 부족 비율)·%; 선택된 영양소 줄만 `landing-ink` 650. 아래에 결과 수치와 계산식.
- **서버 로그:** 위에 선을 그은 고정폭 글자 블록, 실제 21:00 스케줄러 형식 그대로.

### 재계산 움직임 (앱 대표 컴포넌트)
- 서버가 다시 계산하면 진행 막대가 `clip-path`로 700ms 동안 자라고, kcal 옆에 증감 칩이 떠올라요. 추천이 바뀌면 제목이 6px 떠오르며 나타나고(450ms), 근거 단계가 위에서 아래로 펼쳐지며(650ms), 새로 고른 영양소 줄이 1.6초 동안 연하게 물들어요. 움직임 줄이기에서는 이동 없이 페이드만 남아요.

## Do's and Don'ts

### Do:
- **Do** 새 화면을 만들기 전에 랜딩 세계(어두운 무대, 설득)인지 앱 세계(밝은 카드, 작업)인지 먼저 정하고 그 세계의 토큰만 써요.
- **Do** 탄수화물·단백질·지방은 언제나 `carb`·`protein`·`fat`(어두운 바닥 글자는 `-ink`)로 표시해요.
- **Do** 랜딩에서는 구획을 1px `landing-line` 머리카락 선과 여백으로만 나눠요.
- **Do** 랜딩의 주 행동은 알약형 버튼 하나(`landing-button-primary`)로 끝내요.
- **Do** 랜딩의 3D 배경색과 CSS 바닥색을 같은 `landing-ground` 값으로 맞춰요.
- **Do** 3D 움직임에는 항상 움직임 줄이기용 정지 장면과 WebGL 없음 대체 그림을 함께 둬요.
- **Do** 수치는 `tabular-nums`, 단위(kcal, g, %, 100g당)를 붙여 써요.
- **Do** 예시 데이터는 실제 시드 값(628kcal, 95.1 / 53.2 / 3.1g, 아몬드 94g)과 실제 로그 형식만 쓰고, 출처를 짧은 캡션으로 밝혀요.
- **Do** 모든 문구를 ~해요 말투로 써요.

### Don't:
- **Don't** 랜딩 세계에 카드, CSS 그림자, 블러/유리 효과를 넣지 않아요. (앱 세계의 흰 카드는 그 세계의 고유 재료이므로 이 금지에 해당하지 않아요.)
- **Don't** 랜딩에 두 번째 버튼이나 첫 화면 버튼, 내비게이션 막대를 두지 않아요.
- **Don't** `protein`(#2f6f4f)을 어두운 바닥 위 글자색으로 쓰지 않아요.
- **Don't** 영양소 색을 영양소가 아닌 의미(성공, 경고, 장식)에 쓰지 않아요.
- **Don't** 한 세계의 바닥색을 다른 세계 화면에 가져오지 않아요.
- **Don't** 지어낸 사용자 수, 후기, 이용 통계를 보여 주지 않아요.
- **Don't** 앱 세계의 미색 바닥을 이 문서를 근거로 바꾸거나 확정하지 않아요. 사용자가 결정할 문제예요.
