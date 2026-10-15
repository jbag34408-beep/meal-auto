import { useState } from "react"
import { ArrowRight } from "lucide-react"
import { AuraBackdrop } from "@/components/aura/aura-backdrop"
import { ThemeToggle } from "@/components/aura/theme-toggle"
import { useAuraTheme } from "@/components/aura/use-aura-theme"
import { CinematicFooter } from "@/components/ui/motion-footer"
import { login } from "@/lib/api"
import "@/components/aura/aura.css"
import "./landing.css"

const DEMO = { username: "demo", password: "demo1234" }

// 소개 화면: 스크롤 없이 한 화면 (배경·테마는 로그인·식단 화면과 같음)
export default function LandingPage() {
  const [theme, setTheme] = useAuraTheme()
  const [demoBusy, setDemoBusy] = useState(false)
  const [demoError, setDemoError] = useState("")

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
    <main className="aura landing" data-theme={theme}>
      <AuraBackdrop theme={theme} />

      <div className="landing-stage">
        <CinematicFooter
          mode="page"
          heading="오늘 먹은 것부터 적어 보세요"
          marquee={["영양 자동 계산", "다음 끼니 추천", "매일 자동 리포트", "탄수화물 · 단백질 · 지방", "AI 문장 기록"]}
          actions={[
            { label: "시작하기", href: "/login.html", primary: true, icon: <ArrowRight className="h-5 w-5" aria-hidden /> },
            { label: demoBusy ? "들어가는 중" : "체험 계정으로 둘러보기", onClick: tryDemo },
          ]}
          links={[
            { label: "로그인", href: "/login.html" },
            { label: "회원가입", href: "/login.html?mode=signup" },
          ]}
          copyright="© 2026 식단 자동화"
          credit={<span>{demoError || "demo / demo1234 로 바로 체험"}</span>}
          corner={<ThemeToggle theme={theme} onChange={setTheme} />}
        />
      </div>
    </main>
  )
}
