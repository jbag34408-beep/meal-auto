import { useEffect, useRef, useState } from "react"
import { gsap } from "gsap"
import { ScrollTrigger } from "gsap/ScrollTrigger"
import { ArrowRight } from "lucide-react"
import { AuraBackdrop } from "@/components/aura/aura-backdrop"
import { ThemeToggle } from "@/components/aura/theme-toggle"
import { useAuraTheme } from "@/components/aura/use-aura-theme"
import { CinematicFooter } from "@/components/ui/motion-footer"
import { login } from "@/lib/api"
import "@/components/aura/aura.css"
import "./landing.css"

gsap.registerPlugin(ScrollTrigger)

const DEMO = { username: "demo", password: "demo1234" }

const STRANDS = [
  { code: "CARB", name: "탄수화물", food: "쌀밥 100g", amount: "45g", text: "먹은 양에 맞춰 비율로 계산해요." },
  { code: "PROTEIN", name: "단백질", food: "닭가슴살 100g", amount: "23g", text: "하루 목표는 회원마다 따로 정해요." },
  { code: "FAT", name: "지방", food: "아몬드 100g", amount: "50g", text: "가장 부족한 영양소부터 채우도록 추천해요." },
]

const CALC_ROWS = [
  ["바나나 120g", "100g당 90kcal × 1.2", "108"],
  ["쌀밥 150g", "100g당 200kcal × 1.5", "300"],
  ["닭가슴살 200g", "100g당 110kcal × 2", "220"],
]

const CHAIN = [
  { name: "탄수화물", value: 68 },
  { name: "단백질", value: 11 },
  { name: "지방", value: 94, pick: true },
]

// 소개 화면 (로그인·식단 화면과 같은 Aura 디자인)
export default function LandingPage() {
  const [theme, setTheme] = useAuraTheme()
  const [demoBusy, setDemoBusy] = useState(false)
  const [demoError, setDemoError] = useState("")
  const contentRef = useRef<HTMLDivElement>(null)

  // 섹션이 화면에 들어오면 아래에서 떠오름 (동작 줄이기 설정이면 생략)
  useEffect(() => {
    if (!contentRef.current || matchMedia("(prefers-reduced-motion: reduce)").matches) return
    const ctx = gsap.context(() => {
      gsap.utils.toArray<HTMLElement>("[data-reveal]").forEach((el) => {
        gsap.from(el, {
          y: 40,
          opacity: 0,
          duration: 0.9,
          ease: "power3.out",
          scrollTrigger: { trigger: el, start: "top 88%", once: true },
        })
      })
    }, contentRef)
    return () => ctx.revert()
  }, [])

  // 체험 계정으로 바로 로그인해서 식단 화면으로
  async function tryDemo() {
    setDemoBusy(true)
    setDemoError("")
    try {
      await login(DEMO.username, DEMO.password)
      location.href = "/index.html"
    } catch (err) {
      setDemoError(err instanceof Error ? err.message : "체험 계정으로 들어가지 못했어요.")
      setDemoBusy(false)
    }
  }

  return (
    <div className="aura landing" data-theme={theme}>
      <AuraBackdrop theme={theme} />

      <div ref={contentRef} className="landing-content">
        <header className="landing-bar">
          <a href="/landing.html" className="landing-brand">
            <span className="landing-mark" aria-hidden>
              <i />
              <i />
              <i />
            </span>
            식단 자동화
          </a>
          <nav className="landing-nav" aria-label="소개 메뉴">
            <a href="#strands">세 가닥</a>
            <a href="#features">기능</a>
          </nav>
          <div className="landing-actions">
            <ThemeToggle theme={theme} onChange={setTheme} />
            <a href="/login.html" className="landing-pill is-dark">
              로그인
            </a>
          </div>
        </header>

        <main>
          <section className="landing-hero" aria-labelledby="hero-title">
            <div className="landing-hero-card">
              <span className="aura-tag">MEAL.AUTO</span>
              <h1 id="hero-title" className="landing-hero-title">
                식단 자동화
              </h1>
              <p className="landing-hero-lede">먹은 음식을 기록하면 영양을 계산하고 다음 끼니를 추천해요.</p>
              <div className="landing-hero-buttons">
                <a href="/login.html" className="aura-submit landing-cta">
                  시작하기
                  <ArrowRight width={18} height={18} strokeWidth={2.2} aria-hidden />
                </a>
                <button type="button" className="landing-pill is-tall" onClick={tryDemo} disabled={demoBusy} aria-busy={demoBusy}>
                  {demoBusy ? "들어가는 중" : "체험 계정으로 둘러보기"}
                </button>
              </div>
              <p className="landing-hero-note" role={demoError ? "alert" : undefined}>
                {demoError || "체험 계정 demo / demo1234"}
              </p>
            </div>
            <p className="landing-scroll" aria-hidden>
              SCROLL
              <i />
            </p>
          </section>

          <section id="strands" className="landing-section" aria-labelledby="strands-title">
            <div className="landing-head" data-reveal>
              <span className="aura-tag">THREE.STRANDS</span>
              <h2 id="strands-title">하루 식단은 세 가닥이에요</h2>
              <p>매듭을 풀면 탄수화물, 단백질, 지방이 각자의 목표를 가져요.</p>
            </div>
            <ol className="landing-strands">
              {STRANDS.map((s) => (
                <li key={s.code} className="landing-card landing-strand" data-reveal>
                  <div className="landing-strand-head">
                    <span className="landing-code">{s.code}</span>
                    <h3>{s.name}</h3>
                  </div>
                  <p className="landing-strand-amount">
                    <b>{s.amount}</b>
                    <span>{s.food}</span>
                  </p>
                  <p className="landing-strand-text">{s.text}</p>
                </li>
              ))}
            </ol>
          </section>

          <section id="features" className="landing-section" aria-labelledby="features-title">
            <div className="landing-head" data-reveal>
              <span className="aura-tag">AUTOMATION</span>
              <h2 id="features-title">세 가지를 자동으로 해요</h2>
            </div>
            <div className="landing-features">
              <article className="landing-card landing-feature" data-reveal>
                <span className="landing-code">01 · CALC</span>
                <h3>영양 자동 계산</h3>
                <p className="landing-feature-text">음식과 양만 고르면 100g 기준 값을 먹은 양으로 환산해 하루 합계를 내요.</p>
                <p className="landing-basis">demo 계정의 하루 기록 예시</p>
                <ul className="landing-rows">
                  {CALC_ROWS.map(([food, how, kcal]) => (
                    <li key={food}>
                      <span>{food}</span>
                      <span className="landing-how">{how}</span>
                      <b>{kcal} kcal</b>
                    </li>
                  ))}
                  <li className="is-total">
                    <span>하루 합계</span>
                    <span className="landing-how" />
                    <b>628 kcal</b>
                  </li>
                </ul>
                <p className="landing-macros">탄수화물 95.1g · 단백질 53.2g · 지방 3.1g</p>
              </article>

              <article className="landing-card landing-feature" data-reveal>
                <span className="landing-code">02 · SUGGEST</span>
                <h3>다음 끼니 추천</h3>
                <p className="landing-feature-text">
                  목표 대비 가장 부족한 영양소를 찾고, 그 영양소가 가장 많은 음식과 양을 골라요. 남은 칼로리는 넘지 않아요.
                </p>
                <p className="landing-basis">같은 하루, 목표 대비 부족한 비율</p>
                <ul className="landing-chain">
                  {CHAIN.map((c) => (
                    <li key={c.name} className={c.pick ? "is-pick" : ""}>
                      <span>{c.name}</span>
                      <span className="landing-track">
                        <span style={{ width: `${c.value}%` }} />
                      </span>
                      <span>{c.value}%</span>
                    </li>
                  ))}
                </ul>
                <div className="landing-result">
                  <b>아몬드 94g</b>
                  <span>지방 46.9g ÷ 100g당 50g × 100 = 94g</span>
                </div>
              </article>

              <article className="landing-card landing-feature is-wide" data-reveal>
                <div>
                  <span className="landing-code">03 · REPORT</span>
                  <h3>매일 자동 리포트</h3>
                  <p className="landing-feature-text">매일 밤 9시(한국 시간), 회원마다 그날의 요약과 추천을 서버 로그로 남겨요.</p>
                </div>
                <div>
                  <p className="landing-basis">서버 로그 예시</p>
                  <pre className="landing-log">
                    <span>21:00</span> [demo / 2026-10-13 요약] 식사 3회, 628.0 / 2000.0 kcal, 탄수화물 95.1g, 단백질 53.2g, 지방 3.1g
                    {"\n"}
                    <span>21:00</span> [demo 추천] 아몬드 94.0g - 지방이 목표보다 46.9g 부족해요
                  </pre>
                </div>
              </article>
            </div>
          </section>
        </main>
      </div>

      <CinematicFooter
        heading="오늘 먹은 것부터 적어 보세요"
        giantText="MEAL AUTO"
        marquee={["영양 자동 계산", "다음 끼니 추천", "매일 자동 리포트", "탄수화물 · 단백질 · 지방", "AI 문장 기록"]}
        actions={[
          { label: "시작하기", href: "/login.html", primary: true, icon: <ArrowRight className="h-5 w-5" aria-hidden /> },
          { label: demoBusy ? "들어가는 중" : "체험 계정으로 둘러보기", onClick: tryDemo },
        ]}
        links={[
          { label: "세 가닥", href: "#strands" },
          { label: "기능", href: "#features" },
          { label: "로그인", href: "/login.html" },
        ]}
        copyright="© 2026 식단 자동화"
        credit={<span>demo / demo1234 로 바로 체험</span>}
      />
    </div>
  )
}
