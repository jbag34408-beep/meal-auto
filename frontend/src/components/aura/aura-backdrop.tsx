import { Component, lazy, Suspense, useEffect, useState, type ReactNode } from "react"
import type { AuraTheme } from "./chrome-blob"

// three.js 코드는 이 화면에서만, 필요할 때 따로 받아 옴
const ChromeBlob = lazy(() => import("./chrome-blob"))

function canUseWebGL() {
  try {
    const canvas = document.createElement("canvas")
    return !!(canvas.getContext("webgl2") || canvas.getContext("webgl"))
  } catch {
    return false
  }
}

function usePrefersReducedMotion() {
  const [reduced, setReduced] = useState(
    () => typeof matchMedia === "function" && matchMedia("(prefers-reduced-motion: reduce)").matches
  )
  useEffect(() => {
    if (typeof matchMedia !== "function") return
    const mq = matchMedia("(prefers-reduced-motion: reduce)")
    const sync = () => setReduced(mq.matches)
    mq.addEventListener("change", sync)
    return () => mq.removeEventListener("change", sync)
  }, [])
  return reduced
}

// 3D 장면이 실패해도 대체 배경은 그대로 남도록
class SceneBoundary extends Component<{ children: ReactNode }, { failed: boolean }> {
  state = { failed: false }
  static getDerivedStateFromError() {
    return { failed: true }
  }
  render() {
    return this.state.failed ? null : this.props.children
  }
}

// 화면 전체 배경: CSS 그라데이션을 먼저 깔고, 준비되면 크롬 덩어리를 서서히 띄움
export function AuraBackdrop({ theme }: { theme: AuraTheme }) {
  const reducedMotion = usePrefersReducedMotion()
  const [webgl] = useState(canUseWebGL)
  const [ready, setReady] = useState(false)

  return (
    <div className="aura-backdrop" aria-hidden>
      <div className="aura-backdrop-fallback" />
      {webgl && (
        <div className="aura-backdrop-scene" data-ready={ready}>
          <SceneBoundary>
            <Suspense fallback={null}>
              <ChromeBlob theme={theme} reducedMotion={reducedMotion} onReady={() => setReady(true)} />
            </Suspense>
          </SceneBoundary>
        </div>
      )}
      <div className="aura-backdrop-haze" />
    </div>
  )
}
