import { useState } from "react"
import { ArrowRight } from "lucide-react"
import {
  addMeal,
  currentMealType,
  fmt,
  MEAL_NAMES,
  type MealType,
  type Me,
  type Recommendation,
  type Summary,
} from "@/lib/meal-api"
import { Bar, Card, MealPicker, NoticeLine, type Notice } from "./parts"

type Props = {
  me: Me
  rec: Recommendation | null
  summary: Summary
  date: string
  isPast: boolean
  onSaved: () => Promise<void>
}

const PER100_KEY = { 탄수화물: "carbohydrate", 단백질: "protein", 지방: "fat" } as const

// 추천이 없을 때 이유 (서버는 칼로리·세 영양소를 다 채웠거나, 고를 음식이 없으면 추천하지 않음)
function emptyMessage(me: Me, s: Summary, day: string) {
  if (s.totalKcal >= me.targetKcal) return `${day} 목표 칼로리 ${fmt(me.targetKcal)}kcal를 다 채웠어요. 더 추천할 음식은 없어요.`
  const macrosMet =
    s.totalCarbohydrate >= me.targetCarbohydrate && s.totalProtein >= me.targetProtein && s.totalFat >= me.targetFat
  if (macrosMet) return `${day} 탄수화물·단백질·지방 목표를 모두 채웠어요.`
  return "지금은 따로 추천할 음식이 없어요. 남은 영양소가 아주 적거나, 그 영양소가 든 음식이 아직 등록되지 않았어요."
}

// 다음 끼니 추천: 가장 부족한 영양소를 채우는 음식 하나와 그 계산 과정
export function RecommendCard({ me, rec, summary, date, isPast, onSaved }: Props) {
  const [mealType, setMealType] = useState<MealType>(currentMealType)
  const [busy, setBusy] = useState(false)
  const [notice, setNotice] = useState<Notice>(null)
  const day = isPast ? "이날" : "오늘"

  async function record() {
    if (!rec) return
    setBusy(true)
    setNotice(null)
    try {
      await addMeal({ date, mealType, foodId: rec.food.id, grams: rec.grams })
      await onSaved()
      setNotice({
        kind: "info",
        text: `${rec.food.name} ${fmt(rec.grams)}g을 ${MEAL_NAMES[mealType]}으로 기록했어요. 요약과 추천을 다시 계산했어요.`,
      })
    } catch (err) {
      setNotice({ kind: "error", text: err instanceof Error ? err.message : "기록하지 못했어요." })
    } finally {
      setBusy(false)
    }
  }

  const picked = rec?.shortfalls.find((s) => s.nutrient === rec.nutrient)
  const per100 = rec ? rec.food[PER100_KEY[rec.nutrient as keyof typeof PER100_KEY] ?? "protein"] : 0

  return (
    <Card
      tag="AI.SUGGEST"
      title={isPast ? "이날 더 먹었으면 좋았을 음식" : "다음 끼니 추천"}
      sub={rec ? `${day} 가장 부족한 ${rec.nutrient}을 채워 줄 음식이에요.` : undefined}
    >
      {!rec ? (
        <p className="meal-empty is-left">{emptyMessage(me, summary, day)}</p>
      ) : (
        <>
          <div className="meal-suggest">
            <div className="meal-suggest-text">
              <b>{rec.food.name}</b>
              <span>
                {fmt(rec.grams)}g · 약 {fmt(Math.round(rec.kcal))} kcal
              </span>
            </div>
            <span className="meal-badge">
              {rec.nutrient} +{fmt(Math.round((per100 * rec.grams) / 100))}g
            </span>
          </div>
          <p className="meal-suggest-reason">{rec.reason}</p>

          <ul className="meal-shortfalls" aria-label="목표 대비 부족한 영양소">
            {rec.shortfalls.map((s) => (
              <li key={s.nutrient} className={s.nutrient === rec.nutrient ? "is-pick" : ""}>
                <span className="meal-shortfall-name">{s.nutrient}</span>
                <Bar percent={s.ratio * 100} />
                <span className="meal-shortfall-detail">
                  {s.lack > 0 ? `${fmt(s.lack)}g 부족` : "채움"}
                </span>
              </li>
            ))}
          </ul>

          <details className="meal-calc">
            <summary>추천량 계산 보기</summary>
            <p>
              {rec.food.name} 100g당 {rec.nutrient} {fmt(per100)}g → {fmt(picked?.lack ?? 0)}g ÷ {fmt(per100)}g × 100 ={" "}
              {fmt(rec.neededGrams)}g
            </p>
            <p>
              {rec.capped
                ? `남은 칼로리 ${fmt(Math.round(rec.remainingKcal))}kcal에 맞춰 ${fmt(rec.grams)}g으로 줄였어요.`
                : `남은 칼로리 ${fmt(Math.round(rec.remainingKcal))}kcal 안이라 그대로 추천해요.`}
            </p>
          </details>

          <MealPicker label="기록할 끼니" value={mealType} onChange={setMealType} disabled={busy} />
          <button type="button" className="aura-submit meal-submit is-wide" onClick={record} disabled={busy} aria-busy={busy}>
            {busy ? (
              <span className="aura-spinner" aria-label="기록하는 중" />
            ) : (
              <>
                이대로 기록
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
