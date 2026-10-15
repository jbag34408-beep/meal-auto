import { useCallback, useEffect, useState } from "react"
import { ChevronLeft, ChevronRight } from "lucide-react"
import { AuraBackdrop } from "@/components/aura/aura-backdrop"
import { ThemeToggle } from "@/components/aura/theme-toggle"
import { useAuraTheme } from "@/components/aura/use-aura-theme"
import {
  getFoods,
  getMe,
  getMeals,
  getMealPlan,
  getSummary,
  logout,
  shiftDate,
  todayKst,
  type Food,
  type Me,
  type MealEntry,
  type MealPlanResponse,
  type Summary,
} from "@/lib/meal-api"
import { LogCard } from "./LogCard"
import { ManageCard } from "./ManageCard"
import { MealPlanCard } from "./MealPlanCard"
import { RecordsCard } from "./RecordsCard"
import { SummaryCard } from "./SummaryCard"
import "@/components/aura/aura.css"
import "./meal.css"

type Day = { meals: MealEntry[]; summary: Summary; plan: MealPlanResponse | null; animate: boolean }

const today = todayKst()

function dateTexts(date: string) {
  const d = new Date(`${date}T00:00:00`)
  const weekday = d.toLocaleDateString("ko-KR", { weekday: "long" })
  const isToday = date === today
  return {
    main: date.replaceAll("-", ". "),
    sub: isToday ? `${weekday} · 오늘` : weekday,
    dayLabel: isToday
      ? "오늘 먹은 양"
      : `${d.toLocaleDateString("ko-KR", { month: "long", day: "numeric", weekday: "short" })} 먹은 양`,
  }
}

// 오늘 식단 화면 (로그인 화면과 같은 Aura 디자인)
export default function MealPage() {
  const [theme, setTheme] = useAuraTheme()
  const [date, setDate] = useState(today)
  const [me, setMe] = useState<Me | null>(null)
  const [foods, setFoods] = useState<Food[]>([])
  const [day, setDay] = useState<Day | null>(null)
  const [loadError, setLoadError] = useState("")

  // animate: 내가 방금 바꾼 기록 때문에 다시 계산한 경우에만 변화를 보여 줌 (첫 로딩·날짜 변경은 조용히)
  const loadDay = useCallback(async (target: string, animate = false) => {
    try {
      const [meals, summary, plan] = await Promise.all([getMeals(target), getSummary(target), getMealPlan(target)])
      setDay({ meals, summary, plan, animate })
      setLoadError("")
    } catch (err) {
      setLoadError(err instanceof Error ? err.message : "불러오지 못했어요.")
    }
  }, [])

  const loadFoods = useCallback(async () => setFoods(await getFoods()), [])

  useEffect(() => {
    Promise.all([getMe(), getFoods()])
      .then(([m, f]) => {
        setMe(m)
        setFoods(f)
      })
      .catch((err) => setLoadError(err instanceof Error ? err.message : "불러오지 못했어요."))
  }, [])

  useEffect(() => {
    loadDay(date)
  }, [date, loadDay])

  const refresh = () => loadDay(date, true)
  const texts = dateTexts(date)

  function changeDate(next: string) {
    if (!next) return
    setDate(next > today ? today : next)
  }

  return (
    <div className="aura meal" data-theme={theme}>
      <AuraBackdrop theme={theme} />

      <div className="meal-shell">
        <header className="meal-bar">
          <div className="meal-brand">
            <span className="meal-mark" aria-hidden>
              <i />
              <i />
              <i />
            </span>
            <span className="meal-brand-name">식단 자동화</span>
            <span className="aura-tag meal-brand-tag">MEAL.AUTO</span>
          </div>

          <nav className="meal-date" aria-label="날짜 이동">
            <button type="button" className="meal-icon-btn" onClick={() => changeDate(shiftDate(date, -1))} aria-label="전날 보기">
              <ChevronLeft width={18} height={18} aria-hidden />
            </button>
            <label className="meal-date-text">
              <span className="meal-date-main">{texts.main}</span>
              <span className="meal-date-sub">{texts.sub}</span>
              <input
                type="date"
                value={date}
                max={today}
                onChange={(e) => changeDate(e.target.value)}
                aria-label="날짜 고르기"
              />
            </label>
            <button
              type="button"
              className="meal-icon-btn"
              onClick={() => changeDate(shiftDate(date, 1))}
              disabled={date >= today}
              aria-label="다음 날 보기"
            >
              <ChevronRight width={18} height={18} aria-hidden />
            </button>
          </nav>

          <div className="meal-account">
            {me && <span className="meal-user">{me.username}님</span>}
            <ThemeToggle theme={theme} onChange={setTheme} />
            <button type="button" className="meal-pill-btn" onClick={logout}>
              로그아웃
            </button>
          </div>
        </header>

        {loadError && (
          <p className="meal-load-error" role="alert">
            {loadError}
          </p>
        )}

        {me && day ? (
          <main className="meal-grid">
            <div className="meal-col">
              <SummaryCard me={me} summary={day.summary} dayLabel={texts.dayLabel} animate={day.animate} />
              <LogCard date={date} foods={foods} onSaved={refresh} onFoodsChanged={loadFoods} />
              <RecordsCard meals={day.meals} isToday={date === today} onChanged={refresh} />
            </div>
            <div className="meal-col is-side">
              <MealPlanCard
                key={date}
                plan={day.plan}
                date={date}
                isPast={date < today}
                onSaved={refresh}
              />
              <ManageCard
                me={me}
                onTargetsSaved={async () => {
                  setMe(await getMe())
                  await refresh()
                }}
                onFoodsChanged={loadFoods}
              />
            </div>
          </main>
        ) : (
          !loadError && (
            <div className="meal-loading" role="status">
              <span className="aura-spinner" aria-hidden />
              불러오는 중
            </div>
          )
        )}
      </div>
    </div>
  )
}
