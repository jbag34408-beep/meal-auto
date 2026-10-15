import { useEffect, useRef, useState } from "react"
import { fmt, type Me, type Summary } from "@/lib/meal-api"
import { Bar, Divider } from "./parts"

type Props = {
  me: Me
  summary: Summary
  dayLabel: string
  animate: boolean // 내가 방금 바꾼 기록 때문에 다시 계산했을 때만 숫자를 굴림
}

const reduceMotion = () => typeof matchMedia === "function" && matchMedia("(prefers-reduced-motion: reduce)").matches

// 이전 값에서 새 값으로 숫자를 굴림 (ease-out expo, 0.7초)
function useTweened(value: number, animate: boolean) {
  const [shown, setShown] = useState(value)
  const from = useRef(value)

  useEffect(() => {
    const start = from.current
    from.current = value
    if (!animate || reduceMotion() || start === value) {
      setShown(value)
      return
    }
    let frame = 0
    const t0 = performance.now()
    const step = (now: number) => {
      const t = Math.min(1, (now - t0) / 700)
      const eased = t === 1 ? 1 : 1 - Math.pow(2, -10 * t)
      setShown(Math.round(start + (value - start) * eased))
      if (t < 1) frame = requestAnimationFrame(step)
    }
    frame = requestAnimationFrame(step)
    return () => cancelAnimationFrame(frame)
  }, [value, animate])

  return shown
}

// 바뀐 양을 잠깐 보여 주는 표시 (+120 / −80)
function useDelta(value: number, animate: boolean) {
  const prev = useRef(value)
  const [delta, setDelta] = useState<{ diff: number; key: number } | null>(null)
  useEffect(() => {
    const diff = value - prev.current
    prev.current = value
    if (animate && diff !== 0) setDelta((d) => ({ diff, key: (d?.key ?? 0) + 1 }))
  }, [value, animate])
  return delta
}

export function SummaryCard({ me, summary, dayLabel, animate }: Props) {
  const kcal = Math.round(summary.totalKcal)
  const shown = useTweened(kcal, animate)
  const delta = useDelta(kcal, animate)
  const percent = me.targetKcal > 0 ? (kcal / me.targetKcal) * 100 : 0
  const remaining = Math.max(0, Math.round(me.targetKcal - kcal))

  const macros = [
    { code: "CARB", name: "탄수화물", value: summary.totalCarbohydrate, target: me.targetCarbohydrate },
    { code: "PROTEIN", name: "단백질", value: summary.totalProtein, target: me.targetProtein },
    { code: "FAT", name: "지방", value: summary.totalFat, target: me.targetFat },
  ]

  return (
    <section className="meal-card meal-summary" aria-label={dayLabel}>
      <div className="meal-summary-top">
        <div className="meal-intake">
          <span className="aura-tag">DAILY.INTAKE</span>
          <p className="meal-intake-label">{dayLabel}</p>
          <p className="meal-kcal">
            <span className="meal-kcal-num">{fmt(shown)}</span>
            <span className="meal-kcal-unit">kcal</span>
            {delta && (
              <span key={delta.key} className={`meal-delta ${delta.diff < 0 ? "is-minus" : ""}`} aria-hidden>
                {delta.diff > 0 ? "+" : "−"}
                {fmt(Math.abs(delta.diff))}
              </span>
            )}
          </p>
        </div>
        <dl className="meal-goal">
          <div>
            <dt>목표</dt>
            <dd>{fmt(Math.round(me.targetKcal))} kcal</dd>
          </div>
          <div>
            <dt>남은 양</dt>
            <dd>{fmt(remaining)} kcal</dd>
          </div>
          <Bar percent={percent} thick />
          <p className="meal-goal-note">
            {Math.round(percent)}% 달성 · {summary.mealCount}개 기록
          </p>
        </dl>
      </div>

      <Divider label="MACRONUTRIENTS" />

      <ul className="meal-macros" aria-label="영양소 목표 달성">
        {macros.map((m) => {
          const p = m.target > 0 ? (m.value / m.target) * 100 : 0
          return (
            <li key={m.code} className="meal-macro">
              <div className="meal-macro-head">
                <span className="meal-macro-code">{m.code}</span>
                <span className="meal-macro-name">{m.name}</span>
              </div>
              <p className="meal-macro-value">
                <b>{fmt(m.value)}</b>
                <span>/ {fmt(m.target)} g</span>
              </p>
              <Bar percent={p} />
              <span className="meal-macro-percent">{Math.round(p)}%</span>
            </li>
          )
        })}
      </ul>
    </section>
  )
}
