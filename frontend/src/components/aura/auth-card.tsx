import { useEffect, useId, useRef, useState, type FormEvent } from "react"
import { ArrowRight } from "lucide-react"
import { AppleIcon, GitHubIcon, GoogleIcon } from "./brand-icons"

export type AuthMode = "login" | "signup"

type Props = {
  mode: AuthMode
  onModeChange: (mode: AuthMode) => void
  busy: boolean
  error: string
  onSubmit: (username: string, password: string) => void
  onDemo: () => void
}

const COPY = {
  login: {
    title: "Authenticate",
    subtitle: "Secure bio-metric or credential access.",
    submit: "INITIALIZE UPLINK",
    switchPrompt: "No account?",
    switchAction: "Create one",
  },
  signup: {
    title: "Register",
    subtitle: "Issue a new credential for this terminal.",
    submit: "CREATE IDENTITY",
    switchPrompt: "Already registered?",
    switchAction: "Sign in",
  },
} as const

const SOCIALS = [
  { name: "Apple", Icon: AppleIcon },
  { name: "Google", Icon: GoogleIcon },
  { name: "GitHub", Icon: GitHubIcon },
] as const

// 오른쪽 유리 카드: 로그인 ↔ 가입을 같은 카드에서 바꿈
export function AuthCard({ mode, onModeChange, busy, error, onSubmit, onDemo }: Props) {
  const copy = COPY[mode]
  const ids = { user: useId(), pass: useId(), error: useId() }
  const [notice, setNotice] = useState(false)
  const noticeTimer = useRef<number | undefined>(undefined)

  useEffect(() => () => window.clearTimeout(noticeTimer.current), [])

  // 소셜 로그인은 서버에 아직 없음 → 안내만 잠깐
  function showSocialNotice() {
    setNotice(true)
    window.clearTimeout(noticeTimer.current)
    noticeTimer.current = window.setTimeout(() => setNotice(false), 2600)
  }

  function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const data = new FormData(e.currentTarget)
    onSubmit(String(data.get("username") ?? "").trim(), String(data.get("password") ?? ""))
  }

  return (
    <section className="aura-card" aria-labelledby="aura-card-title">
      <span className="aura-tag">AURA.IDENTITY</span>
      <h1 id="aura-card-title" className="aura-title">
        {copy.title}
      </h1>
      <p className="aura-subtitle">{copy.subtitle}</p>

      <div className="aura-socials">
        {SOCIALS.map(({ name, Icon }) => (
          <button
            key={name}
            type="button"
            className="aura-social"
            aria-label={`Continue with ${name}`}
            onClick={showSocialNotice}
          >
            <Icon width={22} height={22} />
          </button>
        ))}
      </div>
      <p className="aura-notice" role="status" data-show={notice}>
        {notice ? "Social sign-in is coming soon." : ""}
      </p>

      <div className="aura-divider" aria-hidden>
        <span>STANDARD PROTOCOL</span>
      </div>

      <form onSubmit={handleSubmit}>
        <label htmlFor={ids.user} className="aura-label">
          IDENTIFIER
        </label>
        <input
          id={ids.user}
          name="username"
          className="aura-input"
          placeholder="username"
          autoComplete="username"
          autoCapitalize="none"
          spellCheck={false}
          required
          disabled={busy}
          aria-describedby={error ? ids.error : undefined}
        />

        <label htmlFor={ids.pass} className="aura-label aura-label-next">
          PASSCODE
        </label>
        <input
          id={ids.pass}
          name="password"
          type="password"
          className="aura-input"
          placeholder="••••••••••••"
          autoComplete={mode === "login" ? "current-password" : "new-password"}
          required
          disabled={busy}
          aria-invalid={!!error}
          aria-describedby={error ? ids.error : undefined}
        />

        <p id={ids.error} className="aura-error" role="alert">
          {error}
        </p>

        <button type="submit" className="aura-submit" disabled={busy} aria-busy={busy}>
          {busy ? (
            <span className="aura-spinner" aria-label="Processing" />
          ) : (
            <>
              {copy.submit}
              <ArrowRight width={18} height={18} strokeWidth={2.2} aria-hidden />
            </>
          )}
        </button>
      </form>

      <div className="aura-links">
        <p>
          {copy.switchPrompt}{" "}
          <button
            type="button"
            className="aura-link"
            disabled={busy}
            onClick={() => onModeChange(mode === "login" ? "signup" : "login")}
          >
            {copy.switchAction}
          </button>
        </p>
        <button type="button" className="aura-link aura-link-muted" disabled={busy} onClick={onDemo}>
          Use demo access (demo / demo1234)
        </button>
      </div>
    </section>
  )
}
