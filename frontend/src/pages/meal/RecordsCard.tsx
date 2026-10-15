import { useState } from "react"
import { deleteMeal, fmt, MEAL_NAMES, MEAL_ORDER, mealKcal, type MealEntry } from "@/lib/meal-api"
import { Card } from "./parts"

type Props = {
  meals: MealEntry[]
  isToday: boolean
  onChanged: () => Promise<void>
}

const macroText = (m: MealEntry) => {
  const g = (per100: number) => fmt(Math.round((per100 * m.grams) / 100))
  return `탄 ${g(m.food.carbohydrate)} · 단 ${g(m.food.protein)} · 지 ${g(m.food.fat)}`
}

// 기록: 끼니별로 묶고 끼니마다 칼로리 합계
export function RecordsCard({ meals, isToday, onChanged }: Props) {
  const [error, setError] = useState("")
  const total = meals.reduce((sum, m) => sum + mealKcal(m), 0)

  async function remove(m: MealEntry) {
    if (!confirm(`${m.food.name} 기록을 삭제할까요?`)) return
    setError("")
    try {
      await deleteMeal(m.id)
      await onChanged()
    } catch (err) {
      setError(err instanceof Error ? err.message : "삭제하지 못했어요.")
    }
  }

  return (
    <Card
      tag="TODAY.LOG"
      title="기록"
      right={meals.length > 0 && <span className="meal-card-aside">{meals.length}개 · {fmt(total)} kcal</span>}
    >
      {meals.length === 0 ? (
        <p className="meal-empty">아직 기록이 없어요.{isToday && " 위에서 먹은 걸 적어 보세요."}</p>
      ) : (
        <div className="meal-groups">
          {MEAL_ORDER.map((type) => {
            const group = meals.filter((m) => m.mealType === type)
            if (group.length === 0) return null
            const sum = group.reduce((s, m) => s + mealKcal(m), 0)
            return (
              <div key={type} className="meal-group">
                <div className="meal-group-head">
                  <h3>{MEAL_NAMES[type]}</h3>
                  <span>{fmt(sum)} kcal</span>
                </div>
                <ul className="meal-rows">
                  {group.map((m) => (
                    <li key={m.id} className="meal-row">
                      <span className="meal-row-food">
                        <b>{m.food.name}</b>
                        {m.food.source === "AI" && (
                          <span className="meal-ai-tag" title="AI가 추정한 영양 정보예요">
                            AI 추정
                          </span>
                        )}
                        <span className="meal-row-grams">{fmt(m.grams)}g</span>
                      </span>
                      <span className="meal-row-macros">{macroText(m)}</span>
                      <span className="meal-row-kcal">{fmt(mealKcal(m))} kcal</span>
                      <button
                        type="button"
                        className="meal-row-del"
                        onClick={() => remove(m)}
                        aria-label={`${m.food.name} 기록 삭제`}
                      >
                        삭제
                      </button>
                    </li>
                  ))}
                </ul>
              </div>
            )
          })}
        </div>
      )}
      {error && (
        <p className="meal-notice is-error" role="alert">
          {error}
        </p>
      )}
    </Card>
  )
}
