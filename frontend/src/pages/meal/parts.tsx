import { useId, type ReactNode } from "react"
import { MEAL_NAMES, MEAL_ORDER, type MealType } from "@/lib/meal-api"

// 식단 화면 카드들이 같이 쓰는 작은 조각 (로그인 카드와 같은 Aura 모양)

type CardProps = {
  tag: string
  title: ReactNode
  sub?: ReactNode
  right?: ReactNode
  className?: string
  children: ReactNode
}

// 유리 카드: 위에 모노 태그, 제목, 부제
export function Card({ tag, title, sub, right, className, children }: CardProps) {
  const titleId = useId()
  return (
    <section className={`meal-card ${className ?? ""}`} aria-labelledby={titleId}>
      <div className="meal-card-top">
        <span className="aura-tag">{tag}</span>
        {right}
      </div>
      <h2 id={titleId} className="meal-card-title">
        {title}
      </h2>
      {sub && <p className="meal-card-sub">{sub}</p>}
      {children}
    </section>
  )
}

export function Divider({ label }: { label: string }) {
  return (
    <div className="aura-divider meal-divider" aria-hidden>
      <span>{label}</span>
    </div>
  )
}

type PickerProps = { label: string; value: MealType; onChange: (value: MealType) => void; disabled?: boolean }

// 끼니 고르기: 알약 모양 라디오 버튼
export function MealPicker({ label, value, onChange, disabled }: PickerProps) {
  const name = useId()
  return (
    <fieldset className="meal-pick" disabled={disabled}>
      <legend className="meal-pick-label">{label}</legend>
      {MEAL_ORDER.map((type) => (
        <label key={type} className="meal-chip">
          <input
            type="radio"
            name={name}
            value={type}
            checked={value === type}
            onChange={() => onChange(type)}
          />
          <span>{MEAL_NAMES[type]}</span>
        </label>
      ))}
    </fieldset>
  )
}

export type Notice = { kind: "info" | "error"; text: string } | null

// 처리 결과 한 줄 (성공은 회색, 오류는 빨간색)
export function NoticeLine({ notice }: { notice: Notice }) {
  return (
    <p className={`meal-notice ${notice?.kind === "error" ? "is-error" : ""}`} role="status" aria-live="polite">
      {notice?.text ?? ""}
    </p>
  )
}

// 진행 막대 (0~100%)
export function Bar({ percent, thick }: { percent: number; thick?: boolean }) {
  return (
    <div className={`meal-track ${thick ? "is-thick" : ""}`} aria-hidden>
      <div className="meal-track-fill" style={{ width: `${Math.max(0, Math.min(100, percent))}%` }} />
    </div>
  )
}
