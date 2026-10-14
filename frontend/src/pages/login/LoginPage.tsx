import { useState } from "react"
import { SignInCard, type SignInMode } from "@/components/ui/sign-in-card"
import { login, signup } from "@/lib/api"

const DEMO = { username: "demo", password: "demo1234" }

// 로그인·회원가입 화면: 화면 모양은 SignInCard, 서버 요청은 여기서
export default function LoginPage() {
  const [mode, setMode] = useState<SignInMode>("login")
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
    <SignInCard
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
  )
}
