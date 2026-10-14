import { useRef, useState, type FormEvent } from "react"
import { LiquidGlassButton } from "@/components/ui/liquid-glass-button"
import { login, signup } from "@/lib/api"
import { cn } from "@/lib/utils"

type Mode = "login" | "signup"

const DEMO = { username: "demo", password: "demo1234" }

// 로그인·회원가입 화면
export default function LoginPage() {
  const [mode, setMode] = useState<Mode>("login")
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [busy, setBusy] = useState(false)
  const usernameRef = useRef<HTMLInputElement>(null)

  const isLogin = mode === "login"

  function switchMode(next: Mode) {
    setMode(next)
    setError("")
    setPassword("")
    usernameRef.current?.focus()
  }

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

  function onSubmit(e: FormEvent) {
    e.preventDefault()
    run(async () => {
      if (!isLogin) await signup(username, password) // 가입 후 바로 로그인
      await login(username, password)
    })
  }

  return (
    <main className="min-h-svh bg-paper text-ink grid place-items-center px-4 py-10">
      <div className="w-full max-w-[400px]">
        {/* 세 가닥 표식: 탄수화물·단백질·지방 */}
        <div className="mb-6 grid w-[72px] gap-1.5" aria-hidden="true">
          <i className="block h-1.5 w-full rounded-full bg-carb" />
          <i className="block h-1.5 w-[70%] rounded-full bg-protein" />
          <i className="block h-1.5 w-[45%] rounded-full bg-fat" />
        </div>

        <h1 className="text-[40px] font-extrabold leading-tight tracking-[-0.04em]">식단 자동화</h1>
        <p className="mt-2 mb-8 text-[15px] leading-relaxed text-muted">
          먹은 음식을 적으면 영양을 계산하고 다음 끼니를 추천해요.{" "}
          <a href="/landing.html" className="text-ink underline underline-offset-4">
            어떻게 동작하나요?
          </a>
        </p>

        <section className="rounded-[22px] bg-white px-6 pt-2 pb-6 shadow-[0_1px_0_var(--color-line)]">
          <div className="mb-5 grid grid-cols-2 border-b border-line" role="tablist" aria-label="로그인 또는 회원가입">
            {(["login", "signup"] as const).map((m) => (
              <button
                key={m}
                type="button"
                role="tab"
                aria-selected={mode === m}
                onClick={() => switchMode(m)}
                className={cn(
                  "-mb-px border-b-2 px-2 pt-4 pb-3 text-[15px] transition-colors",
                  "focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent",
                  mode === m ? "border-ink font-bold text-ink" : "border-transparent text-muted hover:text-ink"
                )}
              >
                {m === "login" ? "로그인" : "회원가입"}
              </button>
            ))}
          </div>

          <form onSubmit={onSubmit} className="grid gap-2.5" role="tabpanel">
            <input
              ref={usernameRef}
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder={isLogin ? "아이디" : "아이디 (영문·숫자 4~20자)"}
              aria-label="아이디"
              autoComplete="username"
              required
              className="h-12 rounded-xl border border-transparent bg-paper px-4 text-[16px] outline-none focus:border-line focus:bg-white focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent"
            />
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder={isLogin ? "비밀번호" : "비밀번호 (8자 이상)"}
              aria-label="비밀번호"
              autoComplete={isLogin ? "current-password" : "new-password"}
              required
              className="h-12 rounded-xl border border-transparent bg-paper px-4 text-[16px] outline-none focus:border-line focus:bg-white focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent"
            />
            <LiquidGlassButton type="submit" size="lg" block disabled={busy} className="mt-2">
              {busy ? "확인 중…" : isLogin ? "로그인" : "가입하고 시작하기"}
            </LiquidGlassButton>
          </form>

          <p className="mt-3 min-h-5 text-[14px] text-danger" role="alert">
            {error}
          </p>

          <div className="mt-2 flex flex-wrap items-center justify-between gap-3 border-t border-line pt-4">
            <span className="text-[14px] text-muted">
              체험용 계정 <b className="font-semibold text-ink">demo</b> / <b className="font-semibold text-ink">demo1234</b>
            </span>
            <LiquidGlassButton
              type="button"
              size="sm"
              disabled={busy}
              onClick={() => run(() => login(DEMO.username, DEMO.password))}
            >
              체험 계정으로 들어가기
            </LiquidGlassButton>
          </div>
        </section>
      </div>
    </main>
  )
}
