import { useEffect, useState } from "react"
import { Moon, Sun } from "lucide-react"
import { AuraBackdrop } from "@/components/aura/aura-backdrop"
import { AuthCard, type AuthMode } from "@/components/aura/auth-card"
import type { AuraTheme } from "@/components/aura/chrome-blob"
import { login, signup } from "@/lib/api"
import "@/components/aura/aura.css"

const DEMO = { username: "demo", password: "demo1234" }
const THEME_KEY = "aura-theme"

function loadTheme(): AuraTheme {
  try {
    return localStorage.getItem(THEME_KEY) === "dark" ? "dark" : "light"
  } catch {
    return "light"
  }
}

// 로그인·회원가입 화면: 배경·모서리 글씨·카드 배치는 여기서, 서버 요청도 여기서
export default function LoginPage() {
  const [theme, setTheme] = useState<AuraTheme>(loadTheme)
  const [mode, setMode] = useState<AuthMode>("login")
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState("")

  useEffect(() => {
    try {
      localStorage.setItem(THEME_KEY, theme)
    } catch {
      // 저장소를 못 써도 화면 전환은 그대로 동작
    }
    document.documentElement.style.background = theme === "dark" ? "#0c0d0f" : "#eef1f5"
  }, [theme])

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

      <p className="aura-meta aura-meta-tl" aria-hidden>
        SYS.CORE // ON-LINE
      </p>
      <p className="aura-meta aura-meta-bl" aria-hidden>
        UPLINK_ESTABLISHED<span className="aura-cursor">_</span>
      </p>
      <p className="aura-vertical" aria-hidden>
        {"[   TELE   =   PROMPT   -3R   ]"}
      </p>

      <div className="aura-topbar">
        <span className="aura-version">V03.01</span>
        <button
          type="button"
          className="aura-theme"
          aria-label={theme === "light" ? "Switch to dark theme" : "Switch to light theme"}
          onClick={() => setTheme(theme === "light" ? "dark" : "light")}
        >
          {theme === "light" ? (
            <Sun width={14} height={14} strokeWidth={2} aria-hidden />
          ) : (
            <Moon width={14} height={14} strokeWidth={2} aria-hidden />
          )}
          {theme === "light" ? "LIGHT" : "DARK"}
        </button>
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
