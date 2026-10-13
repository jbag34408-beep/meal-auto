---
target: index.html
total_score: 22
max_score: 40
na_heuristics: 
p0_count: 0
p1_count: 2
target_identity: "file:C:\\Users\\kkk\\Desktop\\meal-auto-main\\src\\main\\resources\\static\\index.html"
target_fingerprint: "sha256:778d47feb37a41b122452f0583229c3987fbb0beea0d2aa4edd4e67f0a8e4296"
target_path: "C:\\Users\\kkk\\Desktop\\meal-auto-main\\src\\main\\resources\\static\\index.html"
timestamp: 2026-10-13T11-44-35Z
slug: src-main-resources-static-index-html
---
Method: dual-agent (A: design review sub-agent · B: detector/browser sub-agent)

## Design Health Score — 22/40 (Acceptable)
| # | Heuristic | Score | Key Issue |
|---|---|---|---|
| 1 | Visibility of System Status | 2 | No success feedback on add/save/register (.info unused); title stays "오늘의 식단" on past dates; fake "2000" before load |
| 2 | Match System / Real World | 3 | Natural terms/units; empty day recommends 쌀밥 667g (~1,334kcal); "next meal" offered for past dates |
| 3 | User Control and Freedom | 2 | Delete via native confirm() only, no undo, no edit, no "back to today" |
| 4 | Consistency and Standards | 2 | Only target form has labels; target inputs 12px; per-row kcal computed client-side |
| 5 | Error Prevention | 3 | HTML min/max mirror server; no grams default |
| 6 | Recognition Rather Than Recall | 2 | Placeholder-only labels vanish; recommendation must be re-typed |
| 7 | Flexibility and Efficiency | 1 | No "add recommendation", no food search in select, no recents, no edit |
| 8 | Aesthetic and Minimalist Design | 3 | Clean but 7 equal-weight cards; empty table header before search |
| 9 | Error Recovery | 3 | Specific ~해요 messages but not field-linked, persist after fix/date change, no next step |
| 10 | Help and Documentation | 1 | No explanation of recommendation logic, demo path, or 21:00 report |

## Design Specificity Verdict
Content specific, design category-generic (warm off-white + forest green + 7 identical white cards at 760px). Nothing designed around the core promise "backend logic is visible". Pretendard declared but never loaded.
Detector (index.html, exit 2, 2 findings): low-contrast advisory — h2 "다음 끼니 추천" #6b716d on #e3efe8 = 4.2:1 (lines 9,10,22,35) — real AA miss; cream-palette warning — --bg #f6f4ef (line 9) — mechanically correct, agrees with LLM verdict. login.html: cream-palette x1. Overlay injected successfully in [Human] tab; console matched CLI.

## Priority Issues
1. [P1] Engineering invisible at key moments — silent recalculation (628→928), recommendation reasoning reduced to one sentence (fat 63% vs carb 68% hidden), client-side per-row kcal (line 239). Fix: highlight changed values + delta, recommendation reasoning panel (all shortfalls, winner, kcal cap), "이 추천 추가" button, server-side meal kcal. Cmd: clarify → animate.
2. [P1] MFDS search failure is a dead end — red text + empty table header (lines 122–125, 320–323). Fix: designed key-missing state in ~해요, point to 직접 등록, hide header until results, clear stale state. Cmd: harden.
3. [P2] Missing feedback / stale state — no success messages; errors persist after fix and date change; static title; fake 2000 on first paint. Fix: .info + aria-live, clear on input/date change, date-derived title, skeleton. Cmd: harden → polish.
4. [P2] Placeholder-only fields & a11y — 6 unlabeled inputs (lines 83–90, 132–136), truncated placeholders, 12px target inputs (iOS zoom), no aria-live, bars lack role/value, 4.2:1 heading. Fix: visible labels, 16px inputs, aria-describedby, role=progressbar, darker heading. Cmd: clarify → adapt.
5. [P2] Flat hierarchy, generic composition — 7 equal cards, config interleaved with core loop, page ends on a chore. Fix: hero = summary+recommendation+add-recommendation; group add+log; collapse targets/MFDS/custom food into "음식·목표 관리"; load Pretendard. Cmd: layout → typeset.

## Persona Red Flags
Evaluator: can't tell recalculation is server-side; recommendation selection logic hidden; 쌀밥 667g on empty past day reads as bug; report/persistence/BCrypt invisible.
Casey: delete 28×46px, logout 31px tall; delete adjacent to kcal; 12px target inputs zoom; custom-food confirmation off-screen.
Sam: 6 unlabeled inputs; no live announcements; context-less "삭제" buttons; div-only bars.
Jordan: grams meaning unclear; target→recommendation link unexplained; "탄·단·지" abbreviation.

## Minor Observations
Date picker after logout in header; empty day lacks CTA/"오늘로"; bars cap at 100% with no over state, kcal has no bar; inline onclick (240, 337) and inline style (111); unhandled loadDay network failure; imported food not preselected.

## Questions to Consider
1. Should the recommendation's full reasoning be the hero instead of the 40px kcal number?
2. Why isn't the recommendation a button ("추천대로 추가")?
3. Should a missing API key be a designed demo state showing server fallback handling?
4. Does "next meal" make sense for past dates — should the log-only nightly report go there instead?
