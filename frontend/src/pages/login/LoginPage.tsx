import { useState } from "react"
import { AuraBackdrop } from "@/components/aura/aura-backdrop"
import { AuthCard, type AuthMode } from "@/components/aura/auth-card"
import { ThemeToggle } from "@/components/aura/theme-toggle"
import { useAuraTheme } from "@/components/aura/use-aura-theme"
import { login, signup } from "@/lib/api"
import "@/components/aura/aura.css"

const DEMO = { username: "demo", password: "demo1234" }

// 로그인·회원가입 화면: 배경·테마 버튼·카드 배치는 여기서, 서버 요청도 여기서
export default function LoginPage() {
  const [theme, setTheme] = useAuraTheme()
  const [mode, setMode] = useState<AuthMode>("login")
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState("")

  // 로그인에 성공하면 오늘 화면으로 이동
  async function run(task: () => Promise<void>) {
    setError("")
    setBusy(true)
    try {
      await task()
      location.href = "/index.html"
    } catch (err) {
      setError(err instanceof Error ? err.message : "요청을 처리하지 못했어요.")
      setBusy(false)
    }
  }

  return (
    <main className="aura" data-theme={theme}>
      <AuraBackdrop theme={theme} />

      <div className="aura-topbar">
        <ThemeToggle theme={theme} onChange={setTheme} />
      </div>

      <div className="aura-stage">
        <AuthCard
          mode={mode}
          onModeChange={(next) => {
            setMode(next)
            setError("")
          }}
          busy={busy}
          error={error}
          onSubmit={(username, password) =>
            run(async () => {
              if (mode === "signup") await signup(username, password) // 가입 후 바로 로그인
              await login(username, password)
            })
          }
          onDemo={() => run(() => login(DEMO.username, DEMO.password))}
        />
      </div>
    </main>
  )
}
