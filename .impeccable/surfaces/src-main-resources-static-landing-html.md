---
version: 1
slug: "src-main-resources-static-landing-html"
primary_target: "src/main/resources/static/landing.html"
related_targets: []
---

# landing.html

Scope: public landing for meal-auto (식단 자동화). Visitor mode: Persuade.
Audience: course/portfolio evaluators arriving cold, desktop and phone equally (PRODUCT.md).
Job: understand in seconds what the service computes, see that the logic is real, press the single "시작하기" (→ /login.html).
Proof: seed foods and demo-day numbers (628kcal / 95.1 / 53.2 / 3.1), the recommendation chain (지방 94% → 아몬드 94g), the real 21:00 scheduler log format. No invented users or reviews.
Constraints: static HTML in src/main/resources/static, Three.js + GSAP from CDN, ~해요 Korean, index.html and login.html untouched, Java untouched (SecurityConfig needs /landing.html permitted; reported to user, not edited).

## Direction contract

THESIS: One day of eating is three strands twisted into one knot; scrolling unties it into carbohydrate, protein and fat and ties it back at the close. Refuses the category default of a phone mockup plus three feature cards.
OWN-WORLD: green-tinted near-black ground (#0f1311) that becomes a floor under three colored spotlights (carb #d9a441, protein #2f6f4f, fat #b06a3b) casting mixed colored shadows; strands in clearcoated metal between chrome and glass; Pretendard heavy display, plain ink body, no cards, hairline rules only, one pill button.
STORY: see the knot, learn the three nutrients by watching it come apart with real seed numbers, read three working behaviors shown as their real outputs, press 시작하기 with the demo account named.
FIRST VIEWPORT: "식단 자동화" set huge top-left with the one-line login-page description under it; the knot centered over its colored shadows; "스크롤해 보세요" bottom center; no nav, no button above the fold (brief pins one button at the end).
FORM: brief-pinned direction (user specified world and sequence); concept-seed not run, no seed key.
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
