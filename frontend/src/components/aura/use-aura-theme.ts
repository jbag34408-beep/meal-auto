import { useEffect, useState } from "react"
import type { AuraTheme } from "./chrome-blob"

const THEME_KEY = "aura-theme"

function loadTheme(): AuraTheme {
  try {
    return localStorage.getItem(THEME_KEY) === "dark" ? "dark" : "light"
  } catch {
    return "light"
  }
}

// 라이트/다크 테마: 화면끼리 같은 값을 쓰도록 localStorage에 저장
export function useAuraTheme() {
  const [theme, setTheme] = useState<AuraTheme>(loadTheme)

  useEffect(() => {
    try {
      localStorage.setItem(THEME_KEY, theme)
    } catch {
      // 저장소를 못 써도 화면 전환은 그대로 동작
    }
    document.documentElement.style.background = theme === "dark" ? "#0c0d0f" : "#eef1f5"
  }, [theme])

  return [theme, setTheme] as const
}
