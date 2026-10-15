import { Moon, Sun } from "lucide-react"
import type { AuraTheme } from "./chrome-blob"

type Props = { theme: AuraTheme; onChange: (theme: AuraTheme) => void }

// 오른쪽 위 라이트/다크 전환 알약 버튼
export function ThemeToggle({ theme, onChange }: Props) {
  return (
    <button
      type="button"
      className="aura-theme"
      aria-label={theme === "light" ? "어두운 화면으로 바꾸기" : "밝은 화면으로 바꾸기"}
      onClick={() => onChange(theme === "light" ? "dark" : "light")}
    >
      {theme === "light" ? (
        <Sun width={14} height={14} strokeWidth={2} aria-hidden />
      ) : (
        <Moon width={14} height={14} strokeWidth={2} aria-hidden />
      )}
      {theme === "light" ? "LIGHT" : "DARK"}
    </button>
  )
}
