import { useEffect, useState } from "react"
import { ArrowRight } from "lucide-react"
import { addMeals, fmt, MEAL_NAMES, type MealPlan, type MealPlanResponse, type Nutrition } from "@/lib/meal-api"
import { Bar, Card, NoticeLine, type Notice } from "./parts"

type Props = {
  plan: MealPlanResponse | null
  date: string
  isPast: boolean
  onSaved: () => Promise<void>
}

const MACROS: { key: keyof Nutrition; name: string; unit: string }[] = [
  { key: "carbohydrate", name: "탄수화물", unit: "g" },
  { key: "protein", name: "단백질", unit: "g" },
  { key: "fat", name: "지방", unit: "g" },
]

// 상차림 합계가 이 끼니 목표에 얼마나 맞는지 (막대 = 목표 대비 %)
function Fit({ plan }: { plan: MealPlan }) {
  return (
    <ul className="meal-fit" aria-label="목표 대비 영양소">
      {MACROS.map((m) => {
        const target = plan.target[m.key]
        const total = plan.total[m.key]
        const percent = target > 0 ? (total / target) * 100 : 100
        return (
          <li key={m.key}>
            <span className="meal-fit-name">{m.name}</span>
            <Bar percent={percent} />
            <span className="meal-fit-value">
              {fmt(total)}
              <small> / {fmt(target)}g</small>
            </span>
          </li>
        )
      })}
    </ul>
  )
}

// 하루 식단 추천: 아직 안 먹은 아침·점심·저녁마다 한식 상차림 (밥 + 국·찌개 + 주반찬 + 나물 + 김치)
export function MealPlanCard({ plan, date, isPast, onSaved }: Props) {
  const meals = plan?.meals ?? []
  const [selected, setSelected] = useState(0)
  const [busy, setBusy] = useState(false)
  const [notice, setNotice] = useState<Notice>(null)

  // 추천이 다시 계산되면 첫 끼니로
  useEffect(() => setSelected(0), [plan])

  const meal = meals[Math.min(selected, meals.length - 1)]

  async function record() {
    if (!meal) return
    setBusy(true)
    setNotice(null)
    try {
      await addMeals({
        date,
        mealType: meal.mealType,
        items: meal.items.map((i) => ({ foodId: i.food.id, grams: i.grams })),
      })
      await onSaved()
      setNotice({
        kind: "info",
        text: `${MEAL_NAMES[meal.mealType]} 상차림 ${meal.items.length}가지를 기록했어요. 남은 끼니 식단을 다시 계산했어요.`,
      })
    } catch (err) {
      setNotice({ kind: "error", text: err instanceof Error ? err.message : "기록하지 못했어요." })
    } finally {
      setBusy(false)
    }
  }

  return (
    <Card
      tag="MEAL.PLAN"
      title={isPast ? "이날 이렇게 먹었으면 좋았을 식단" : "남은 끼니 식단 추천"}
      sub={
        meals.length > 0
          ? `남은 ${fmt(Math.round(plan!.remaining.kcal))}kcal를 끼니에 나눠 탄수화물·단백질·지방이 고르게 맞는 한식 상차림이에요.`
          : undefined
      }
    >
      {!meal ? (
        <p className="meal-empty is-left">{plan?.message ?? "식단을 계산하고 있어요."}</p>
      ) : (
        <>
          {meals.length > 1 && (
            <div className="meal-plan-tabs" role="tablist" aria-label="끼니 고르기">
              {meals.map((m, i) => (
                <button
                  key={m.mealType}
                  type="button"
                  role="tab"
                  aria-selected={i === selected}
                  className="meal-plan-tab"
                  onClick={() => setSelected(i)}
                >
                  {MEAL_NAMES[m.mealType]}
                  <small>{fmt(Math.round(m.total.kcal))}kcal</small>
                </button>
              ))}
            </div>
          )}

          <div className="meal-plan-head">
            <b>{MEAL_NAMES[meal.mealType]} 상차림</b>
            <span>
              {fmt(Math.round(meal.total.kcal))} / 목표 {fmt(Math.round(meal.target.kcal))} kcal
            </span>
          </div>

          <ul className="meal-plan-items">
            {meal.items.map((item) => (
              <li key={item.food.id}>
                <span className="meal-plan-cat">{item.categoryLabel}</span>
                <span className="meal-plan-food">
                  <b>{item.food.name}</b>
                  {item.recent && <span className="meal-option-tag">최근 먹음</span>}
                </span>
                <span className="meal-plan-grams">{fmt(item.grams)}g</span>
                <span className="meal-plan-kcal">{fmt(Math.round(item.nutrition.kcal))} kcal</span>
              </li>
            ))}
          </ul>

          <Fit plan={meal} />

          <button type="button" className="aura-submit meal-submit is-wide" onClick={record} disabled={busy} aria-busy={busy}>
            {busy ? (
              <span className="aura-spinner" aria-label="기록하는 중" />
            ) : (
              <>
                {MEAL_NAMES[meal.mealType]}으로 상차림 기록
                <ArrowRight width={18} height={18} strokeWidth={2.2} aria-hidden />
              </>
            )}
          </button>
        </>
      )}
      <NoticeLine notice={notice} />
    </Card>
  )
}
