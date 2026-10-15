import { useEffect, useState } from "react"
import { ArrowRight } from "lucide-react"
import {
  addMeal,
  currentMealType,
  fmt,
  MEAL_NAMES,
  type MealType,
  type Me,
  type Recommendation,
  type RecommendationOption,
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

// 추천이 없을 때 이유 (서버는 칼로리·세 영양소를 다 채웠거나, 고를 음식이 없으면 추천하지 않음)
function emptyMessage(me: Me, s: Summary, day: string) {
  if (s.totalKcal >= me.targetKcal) return `${day} 목표 칼로리 ${fmt(me.targetKcal)}kcal를 다 채웠어요. 더 추천할 음식은 없어요.`
  const macrosMet =
    s.totalCarbohydrate >= me.targetCarbohydrate && s.totalProtein >= me.targetProtein && s.totalFat >= me.targetFat
  if (macrosMet) return `${day} 탄수화물·단백질·지방 목표를 모두 채웠어요.`
  return "지금은 따로 추천할 음식이 없어요. 남은 영양소가 아주 적거나, 그 영양소가 든 음식이 아직 등록되지 않았어요."
}

const signed = (n: number) => Math.abs(n).toFixed(2)
const score = (n: number) => n.toFixed(2)

// 고른 후보의 점수가 어떻게 나왔는지
function ScoreDetail({ option }: { option: RecommendationOption }) {
  return (
    <details className="meal-calc">
      <summary>점수 계산 보기</summary>
      <p>
        채운 정도 {signed(option.fillScore)} − 초과 {signed(option.overPenalty)} − 칼로리 부담 {signed(option.kcalPenalty)}
        {option.recentPenalty > 0 && ` − 최근 먹음 ${signed(option.recentPenalty)}`} = <b>{score(option.score)}</b>
      </p>
      <ul className="meal-calc-effects">
        {option.effects.map((e) => (
          <li key={e.nutrient}>
            {e.nutrient} +{fmt(e.added)}g
            {e.filled > 0 && ` (부족분 ${fmt(e.filled)}g 채움${e.over >= 0.5 ? `, ${fmt(e.over)}g 초과` : ""})`}
            {e.filled <= 0 && e.over >= 0.5 && ` (전부 초과)`}
          </li>
        ))}
      </ul>
      <p>
        채운 정도는 가장 부족한 영양소의 부족분 중 채운 비율에 다른 영양소를 채운 비율 × 0.25를 더한 값, 초과는 목표를 넘긴 양의 목표 대비 비율, 칼로리 부담은 남은 칼로리 중 이 양이 차지하는 비율 × 0.3이에요.
        {option.capped && " 부족분을 다 채우는 양이 한 끼로는 많아서 줄였어요 (최대 500g, 하루 목표 칼로리의 40%와 남은 칼로리 중 작은 값까지)."}
      </p>
    </details>
  )
}

// 다음 끼니 추천: 점수 순 후보 2~3개 중 하나를 골라 기록
export function RecommendCard({ me, rec, summary, date, isPast, onSaved }: Props) {
  const [mealType, setMealType] = useState<MealType>(currentMealType)
  const [selected, setSelected] = useState(0)
  const [busy, setBusy] = useState(false)
  const [notice, setNotice] = useState<Notice>(null)
  const day = isPast ? "이날" : "오늘"

  // 추천이 다시 계산되면 1위 후보로 돌아감
  useEffect(() => setSelected(0), [rec])

  const options = rec?.options?.length ? rec.options : []
  const option = options[Math.min(selected, options.length - 1)]

  async function record() {
    if (!option) return
    setBusy(true)
    setNotice(null)
    try {
      await addMeal({ date, mealType, foodId: option.food.id, grams: option.grams })
      await onSaved()
      setNotice({
        kind: "info",
        text: `${option.food.name} ${fmt(option.grams)}g을 ${MEAL_NAMES[mealType]}으로 기록했어요. 요약과 추천을 다시 계산했어요.`,
      })
    } catch (err) {
      setNotice({ kind: "error", text: err instanceof Error ? err.message : "기록하지 못했어요." })
    } finally {
      setBusy(false)
    }
  }

  return (
    <Card
      tag="AI.SUGGEST"
      title={isPast ? "이날 더 먹었으면 좋았을 음식" : "다음 끼니 추천"}
      sub={rec ? `${day} 가장 부족한 ${rec.nutrient}을 채우면서 다른 영양소는 덜 넘기는 순서예요.` : undefined}
    >
      {!rec || !option ? (
        <p className="meal-empty is-left">{emptyMessage(me, summary, day)}</p>
      ) : (
        <>
          <div className="meal-options" role="radiogroup" aria-label="추천 후보">
            {options.map((o, i) => (
              <button
                key={o.food.id}
                type="button"
                role="radio"
                aria-checked={i === selected}
                className="meal-option"
                onClick={() => setSelected(i)}
              >
                <span className="meal-option-rank">{i + 1}</span>
                <span className="meal-option-text">
                  <b>
                    {o.food.name}
                    {o.recent && <span className="meal-option-tag">최근 먹음</span>}
                  </b>
                  <span>
                    {fmt(o.grams)}g · 약 {fmt(Math.round(o.kcal))} kcal
                  </span>
                  <span className="meal-option-summary">{o.summary}</span>
                </span>
                <span className="meal-option-score" aria-label={`점수 ${score(o.score)}`}>
                  {score(o.score)}
                </span>
              </button>
            ))}
          </div>

          <ul className="meal-shortfalls" aria-label="목표 대비 부족한 영양소">
            {rec.shortfalls.map((s) => (
              <li key={s.nutrient} className={s.nutrient === rec.nutrient ? "is-pick" : ""}>
                <span className="meal-shortfall-name">{s.nutrient}</span>
                <Bar percent={s.ratio * 100} />
                <span className="meal-shortfall-detail">{s.lack > 0 ? `${fmt(s.lack)}g 부족` : "채움"}</span>
              </li>
            ))}
          </ul>

          <ScoreDetail option={option} />

          <MealPicker label="기록할 끼니" value={mealType} onChange={setMealType} disabled={busy} />
          <button type="button" className="aura-submit meal-submit is-wide" onClick={record} disabled={busy} aria-busy={busy}>
            {busy ? (
              <span className="aura-spinner" aria-label="기록하는 중" />
            ) : (
              <>
                {option.food.name} {fmt(option.grams)}g 기록
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
