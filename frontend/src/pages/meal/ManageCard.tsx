import { useEffect, useState, type FormEvent, type ReactNode } from "react"
import { ChevronDown } from "lucide-react"
import { createFood, fmt, getPublicFoodStatus, importPublicFood, saveTargets, searchPublicFoods, type Me, type PublicFood } from "@/lib/meal-api"
import { Card } from "./parts"

type Props = {
  me: Me
  onTargetsSaved: () => Promise<void>
  onFoodsChanged: () => Promise<void>
}

type AccProps = { title: string; meta?: string; open: boolean; onToggle: () => void; children: ReactNode }

// 접었다 펴는 알약 줄
function Accordion({ title, meta, open, onToggle, children }: AccProps) {
  return (
    <div className="meal-acc" data-open={open}>
      <button type="button" className="meal-acc-head" aria-expanded={open} onClick={onToggle}>
        <span className="meal-acc-title">{title}</span>
        {meta && <span className="meal-acc-meta">{meta}</span>}
        <ChevronDown className="meal-acc-icon" width={18} height={18} aria-hidden />
      </button>
      {open && <div className="meal-acc-body">{children}</div>}
    </div>
  )
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="meal-field">
      <span className="aura-label meal-field-label">{label}</span>
      {children}
    </label>
  )
}

const errText = (err: unknown) => (err instanceof Error ? err.message : "처리하지 못했어요.")

// 목표와 음식 관리: 하루 목표 / 공공 데이터 검색 / 내 음식 등록
export function ManageCard({ me, onTargetsSaved, onFoodsChanged }: Props) {
  const [open, setOpen] = useState<"target" | "public" | "food" | null>(null)
  const toggle = (key: typeof open) => setOpen(open === key ? null : key)

  const [targetMsg, setTargetMsg] = useState({ error: "", ok: "" })
  const [publicError, setPublicError] = useState("")
  const [results, setResults] = useState<PublicFood[] | null>(null)
  const [searching, setSearching] = useState(false)
  const [imported, setImported] = useState<Set<number>>(new Set())
  const [foodMsg, setFoodMsg] = useState({ error: "", ok: "" })
  const [publicAvailable, setPublicAvailable] = useState<boolean | null>(null)

  // 공공데이터 API 키가 없는 서버면 검색 대신 안내를 보여 줌
  useEffect(() => {
    getPublicFoodStatus()
      .then((s) => setPublicAvailable(s.available))
      .catch(() => setPublicAvailable(false))
  }, [])

  async function handleTargets(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const d = new FormData(e.currentTarget)
    setTargetMsg({ error: "", ok: "" })
    try {
      await saveTargets({
        kcal: Number(d.get("kcal")),
        carbohydrate: Number(d.get("carbohydrate")),
        protein: Number(d.get("protein")),
        fat: Number(d.get("fat")),
      })
      await onTargetsSaved() // 새 목표로 요약과 추천 다시 계산
      setTargetMsg({ error: "", ok: "목표를 저장했어요." })
    } catch (err) {
      setTargetMsg({ error: errText(err), ok: "" })
    }
  }

  async function handleSearch(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const keyword = String(new FormData(e.currentTarget).get("keyword") ?? "").trim()
    if (!keyword) return
    setPublicError("")
    setSearching(true)
    setImported(new Set())
    try {
      setResults(await searchPublicFoods(keyword))
    } catch (err) {
      setResults(null)
      setPublicError(errText(err))
    } finally {
      setSearching(false)
    }
  }

  async function importFood(f: PublicFood, index: number) {
    setPublicError("")
    try {
      await importPublicFood(f)
      setImported((s) => new Set(s).add(index))
      await onFoodsChanged()
    } catch (err) {
      setPublicError(errText(err))
    }
  }

  async function handleFood(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const form = e.currentTarget
    const d = new FormData(form)
    setFoodMsg({ error: "", ok: "" })
    try {
      const saved = await createFood({
        name: String(d.get("name")),
        kcalPer100g: Number(d.get("kcal")),
        carbohydrate: Number(d.get("carbohydrate")),
        protein: Number(d.get("protein")),
        fat: Number(d.get("fat")),
      })
      form.reset()
      await onFoodsChanged()
      setFoodMsg({ error: "", ok: `${saved.name}을 등록했어요. 직접 입력하기에서 고를 수 있어요.` })
    } catch (err) {
      setFoodMsg({ error: errText(err), ok: "" })
    }
  }

  return (
    <Card tag="SETTINGS" title="목표와 음식 관리" className="meal-manage">
      <div className="meal-accs">
        <Accordion
          title="내 하루 목표"
          meta={`${fmt(me.targetKcal)} kcal`}
          open={open === "target"}
          onToggle={() => toggle("target")}
        >
          <form className="meal-form" onSubmit={handleTargets}>
            <div className="meal-form-grid">
              <Field label="칼로리 (kcal)">
                <input name="kcal" type="number" className="aura-input" min={500} max={10000} step={1} defaultValue={me.targetKcal} required />
              </Field>
              <Field label="탄수화물 (g)">
                <input name="carbohydrate" type="number" className="aura-input" min={1} max={1000} step={0.1} defaultValue={me.targetCarbohydrate} required />
              </Field>
              <Field label="단백질 (g)">
                <input name="protein" type="number" className="aura-input" min={1} max={500} step={0.1} defaultValue={me.targetProtein} required />
              </Field>
              <Field label="지방 (g)">
                <input name="fat" type="number" className="aura-input" min={1} max={500} step={0.1} defaultValue={me.targetFat} required />
              </Field>
            </div>
            <button type="submit" className="meal-pill-btn is-dark is-wide">
              목표 저장
            </button>
            <FormMessage {...targetMsg} />
          </form>
        </Accordion>

        <Accordion
          title="공공 데이터에서 음식 찾기"
          meta={publicAvailable === false ? "지금은 사용할 수 없어요" : "식약처 DB"}
          open={open === "public"}
          onToggle={() => toggle("public")}
        >
          {publicAvailable === false ? (
            <p className="meal-empty is-left">
              서버에 공공데이터 API 키가 없어서 지금은 식약처 DB를 검색할 수 없어요. 자주 먹는 기본 음식 60여 가지는
              이미 들어 있으니 "직접 입력하기"에서 골라 보세요. 없는 음식은 아래 "내 음식 등록"으로 추가할 수 있어요.
            </p>
          ) : (
            <>
              <form className="meal-search" onSubmit={handleSearch}>
                <input name="keyword" className="aura-input" placeholder="예: 사과, 김치찌개" required aria-label="찾을 음식 이름" />
                <button type="submit" className="meal-pill-btn is-dark" disabled={searching}>
                  {searching ? "찾는 중" : "검색"}
                </button>
              </form>
              <FormMessage error={publicError} ok="" />
              {results && (
                results.length === 0 ? (
                  <p className="meal-empty">검색 결과가 없어요. 다른 이름으로 찾아보세요.</p>
                ) : (
                  <ul className="meal-results">
                    {results.map((f, i) => (
                      <li key={`${f.name}-${i}`}>
                        <div className="meal-result-text">
                          <b>{f.name}</b>
                          <span>
                            {[f.maker, f.basis && `${f.basis} 기준`].filter(Boolean).join(" · ")}
                            {(f.maker || f.basis) && " · "}
                            {fmt(f.kcal)} kcal · 탄 {fmt(f.carbohydrate)} · 단 {fmt(f.protein)} · 지 {fmt(f.fat)}
                          </span>
                        </div>
                        <button
                          type="button"
                          className="meal-pill-btn is-small"
                          disabled={imported.has(i)}
                          onClick={() => importFood(f, i)}
                        >
                          {imported.has(i) ? "가져옴" : "가져오기"}
                        </button>
                      </li>
                    ))}
                  </ul>
                )
              )}
            </>
          )}
        </Accordion>

        <Accordion title="내 음식 등록" meta="100g 기준" open={open === "food"} onToggle={() => toggle("food")}>
          <form className="meal-form" onSubmit={handleFood}>
            <Field label="이름">
              <input name="name" className="aura-input" maxLength={50} required />
            </Field>
            <div className="meal-form-grid">
              <Field label="칼로리 (kcal)">
                <input name="kcal" type="number" className="aura-input" min={0} max={900} step={0.1} required />
              </Field>
              <Field label="탄수화물 (g)">
                <input name="carbohydrate" type="number" className="aura-input" min={0} max={100} step={0.1} required />
              </Field>
              <Field label="단백질 (g)">
                <input name="protein" type="number" className="aura-input" min={0} max={100} step={0.1} required />
              </Field>
              <Field label="지방 (g)">
                <input name="fat" type="number" className="aura-input" min={0} max={100} step={0.1} required />
              </Field>
            </div>
            <button type="submit" className="meal-pill-btn is-dark is-wide">
              등록
            </button>
            <FormMessage {...foodMsg} />
          </form>
        </Accordion>
      </div>
    </Card>
  )
}

function FormMessage({ error, ok }: { error: string; ok: string }) {
  if (!error && !ok) return null
  return (
    <p className={`meal-notice ${error ? "is-error" : ""}`} role={error ? "alert" : "status"}>
      {error || ok}
    </p>
  )
}
