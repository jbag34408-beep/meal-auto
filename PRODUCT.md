# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Primary: people grading a course assignment or reviewing a portfolio, such as a professor or an interviewer. They open the deployed app, sign in with the demo account (`demo` / `demo1234`) or make a new one, and click through the features in a single short session to judge what the developer built. They come equally from desktop and phone browsers, so both have to work properly.

People who log their own meals day to day are a secondary case. The app has to hold up for them, but the product isn't optimized around them.

## Product Purpose

meal-auto (식단 자동화) is a Spring Boot meal-automation service. Users log what they ate. The app calculates calories and carbohydrate/protein/fat, compares the day against each member's personal targets, recommends the next meal, and writes a daily report automatically.

Success means a reviewer finishes a short session convinced the backend is real, correct, and well-engineered. Every feature they touch should visibly work and visibly come from the server: authentication, persistence, calculation, recommendation, and the external public-data integration.

## Positioning

This is a backend engineering showcase delivered as a working product, not a consumer diet app competing on content or polish. What sets it apart is that the reviewer can see the logic at work end to end:
- Per-member targets drive a deterministic recommendation: the macro with the largest relative shortfall picks the food and the gram amount, capped by the remaining kcal.
- Live lookup against the Korean MFDS (식약처) food nutrition public API, with one-click import into the local food DB.
- Spring Security session auth with BCrypt.
- A file-backed H2 database that persists across restarts.
- A scheduled 21:00 KST daily report for every member.

## Operating Context

- Reviewers usually arrive through a shared deployment URL (Docker image, port 8080, `TZ=Asia/Seoul`) or a dev container, then land on `/login.html`.
- A typical demo path: log in → read today's summary and the recommendation → add a meal → see the summary and recommendation recalculate → change targets → search the public food DB and import an item → register a custom food.
- The demo account comes seeded with five foods (쌀밥, 닭가슴살, 바나나, 아몬드, 계란) and three sample meals for today.
- The nightly report currently goes to the server log only; it has no UI.

## Capabilities and Constraints

- Features: signup/login/logout (`/signup`, `/login`, `/logout`, `/me`); per-member daily targets (`PUT /me/targets`); food list and search (`/foods`), custom food creation (per 100g), public API search (`/foods/public?keyword=`); meal entries by date and meal type (BREAKFAST/LUNCH/DINNER/SNACK, i.e. 아침/점심/저녁/간식) with create and delete; daily summary (`/summary`); next-meal recommendation (`/recommendation`, empty when targets are met).
- Stack: Java 17, Spring Boot 4.1.1, Spring Data JPA, Spring Security, H2 file DB, Gradle, Docker. The frontend is plain static HTML/CSS/vanilla JS in `src/main/resources/static/` (`index.html`, `login.html`) with no build step or framework. Future UI work should keep it that way unless the user decides otherwise.
- The public API needs a `FOOD_API_KEY` environment variable. Without the key, or when the call fails (timeout, inactive key), the server returns a Korean error message, so the UI must handle that case gracefully during a demo.
- Validation errors come back as `{ message, errors: { field: msg } }` and the UI displays them.
- Undecided: whether the daily report should ever appear in the UI, for example as a notification or report history.

## Brand Commitments

- Name: 식단 자동화 (meal-auto).
- Language: Korean UI throughout, `lang="ko"`.
- Voice: friendly, polite informal Korean ending in ~해요 (e.g. "아직 기록이 없어요", "오늘 목표를 모두 채웠어요"). Keep this voice in all UI copy and error messages.
- No logo or other brand assets exist.

## Evidence on Hand

- Working features and their tests: `src/test/java/.../service/` (FoodService, MealRecommendService, MealSummaryService, NutritionCalculator).
- Real seeded data: the demo account and five seed foods (`DataInitializer.java`).
- Real external data source: MFDS food nutrition DB (`apis.data.go.kr/1471000/FoodNtrCpntDbInfo02`).
- No users, testimonials, usage metrics, or deployment stats exist. Never invent them.

## Product Principles

1. Make the engineering visible. Each interaction should show a server-side capability working: recalculation, recommendation reasoning, external data, persistence.
2. Keep the demo path short and obvious. A reviewer with a few minutes should reach every feature without instructions.
3. Fail legibly. External API, validation, and auth failures show clear Korean messages and never leave a blank or broken screen.
4. Make the numbers trustworthy. Show values consistently with clear units (kcal, g, per 100g) so the calculations look correct at a glance.
5. Treat desktop and mobile as equals. Every feature must be fully usable on both.
