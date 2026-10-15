import { useRef, useState, type FormEvent } from "react"
import { ArrowRight, ChevronDown } from "lucide-react"
import {
  addMeal,
  analyzeMeal,
  ApiError,
  currentMealType,
  fmt,
  MEAL_NAMES,
  MEAL_ORDER,
  type Food,
  type MealType,
} from "@/lib/meal-api"
import { Card, Divider, MealPicker, NoticeLine, type Notice } from "./parts"

type Props = {
  date: string
  foods: Food[]
  onSaved: () => Promise<void> // 기록이 바뀐 뒤 요약·추천·목록 다시 불러오기
  onFoodsChanged: () => Promise<void>
}

// "무엇을 먹었나요?": AI 문장 기록 + 직접 고르기
export function LogCard({ date, foods, onSaved, onFoodsChanged }: Props) {
  const [mealType, setMealType] = useState<MealType>(currentMealType)
  const [busy, setBusy] = useState(false)
  const [notice, setNotice] = useState<Notice>(null)
  const [manualOpen, setManualOpen] = useState(false)
  const [manualType, setManualType] = useState<MealType>(currentMealType)
  const [manualError, setManualError] = useState("")
  const [highlight, setHighlight] = useState(false)
  const foodRef = useRef<HTMLSelectElement>(null)

  // 문장을 서버에 보내면 서버가 Gemini로 분석해서 음식별로 저장
  async function handleAi(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const form = e.currentTarget
    const text = String(new FormData(form).get("text") ?? "").trim()
    if (!text) return
    setBusy(true)
    setNotice(null)
    setHighlight(false)
    try {
      const saved = await analyzeMeal({ date, mealType, text })
      form.reset()
      await onFoodsChanged() // AI가 새로 만든 음식도 직접 고르기 목록에 나타나게
      await onSaved()
      const list = saved.map((m) => `${m.food.name} ${fmt(m.grams)}g`).join(", ")
      setNotice({
        kind: "info",
        text: `${MEAL_NAMES[mealType]}으로 ${list}을 기록했어요. 양이 다르면 기록을 지우고 직접 추가해 주세요.`,
      })
    } catch (err) {
      setNotice({ kind: "error", text: err instanceof Error ? err.message : "기록하지 못했어요." })
      // AI를 쓸 수 없음(키 없음, 무료 한도 초과 등) → 직접 고르기를 펼쳐서 안내
      if (err instanceof ApiError && err.status === 503) {
        setManualOpen(true)
        setManualType(mealType)
        setHighlight(true)
        requestAnimationFrame(() => foodRef.current?.focus())
      }
    } finally {
      setBusy(false)
    }
  }

  async function handleManual(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const form = e.currentTarget
    const data = new FormData(form)
    setManualError("")
    try {
      await addMeal({
        date,
        mealType: manualType,
        foodId: Number(data.get("food")),
        grams: Number(data.get("grams")),
      })
      ;(form.elements.namedItem("grams") as HTMLInputElement).value = ""
      setHighlight(false)
      await onSaved()
    } catch (err) {
      setManualError(err instanceof Error ? err.message : "기록하지 못했어요.")
    }
  }

  return (
    <Card tag="AI.LOG" title="무엇을 먹었나요?" sub="먹은 걸 문장으로 적으면 AI가 칼로리와 영양소를 계산해요.">
      <form className="meal-ai" onSubmit={handleAi}>
        <input
          name="text"
          className="aura-input"
          placeholder="예: 점심에 김치찌개 한 그릇이랑 공깃밥"
          maxLength={300}
          autoComplete="off"
          required
          disabled={busy}
          aria-label="먹은 음식"
        />
        <button type="submit" className="aura-submit meal-submit" disabled={busy} aria-busy={busy}>
          {busy ? (
            <>
              <span className="aura-spinner" aria-hidden />
              분석 중
            </>
          ) : (
            <>
              AI로 기록
              <ArrowRight width={18} height={18} strokeWidth={2.2} aria-hidden />
            </>
          )}
        </button>
      </form>
      <MealPicker label="끼니" value={mealType} onChange={setMealType} disabled={busy} />
      <NoticeLine notice={notice} />

      <Divider label="MANUAL ENTRY" />

      <div className="meal-acc" data-open={manualOpen}>
        <button
          type="button"
          className="meal-acc-head"
          aria-expanded={manualOpen}
          onClick={() => setManualOpen(!manualOpen)}
        >
          <span className="meal-acc-title">직접 입력하기</span>
          <span className="meal-acc-meta">음식 · 끼니 · 그램</span>
          <ChevronDown className="meal-acc-icon" width={18} height={18} aria-hidden />
        </button>
        {manualOpen && (
          <form className="meal-acc-body meal-manual" onSubmit={handleManual}>
            <select
              ref={foodRef}
              name="food"
              className={`aura-input meal-select ${highlight ? "is-highlight" : ""}`}
              required
              aria-label="음식"
            >
              {foods.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name} (100g당 {fmt(f.kcalPer100g)}kcal{f.servingGrams ? ` · 1인분 ${fmt(f.servingGrams)}g` : ""}){f.shared === false && " · 내 음식"}
                </option>
              ))}
            </select>
            <div className="meal-manual-row">
              <select
                className="aura-input meal-select"
                value={manualType}
                onChange={(e) => setManualType(e.target.value as MealType)}
                aria-label="끼니"
              >
                {MEAL_ORDER.map((t) => (
                  <option key={t} value={t}>
                    {MEAL_NAMES[t]}
                  </option>
                ))}
              </select>
              <input
                name="grams"
                type="number"
                className="aura-input"
                placeholder="먹은 양 (g)"
                min={1}
                max={5000}
                required
                aria-label="먹은 양(g)"
              />
              <button type="submit" className="meal-pill-btn is-dark">
                추가
              </button>
            </div>
            {manualError && (
              <p className="meal-notice is-error" role="alert">
                {manualError}
              </p>
            )}
          </form>
        )}
      </div>
    </Card>
  )
}
