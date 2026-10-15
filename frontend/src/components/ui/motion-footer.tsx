import * as React from "react"
import { useEffect, useRef } from "react"
import { gsap } from "gsap"
import { ScrollTrigger } from "gsap/ScrollTrigger"
import { ArrowUp } from "lucide-react"
import { cn } from "@/lib/utils"

gsap.registerPlugin(ScrollTrigger)

// 원본: 21st.dev "motion-footer" (CinematicFooter)
// 바꾼 점: shadcn 색 변수 대신 Aura 변수(--aura-*)를 쓰고, 문구·링크를 props로 받음.
// .aura 안에서 쓰면 라이트/다크 테마를 그대로 따름.

const STYLES = `
.cinematic-footer {
  --cf-ink: var(--aura-ink, #0b0b0c);
  --cf-bg: var(--aura-bg, #eef1f5);
  --cf-muted: var(--aura-muted, #5d6169);
  --cf-line: var(--aura-line, rgba(11, 11, 12, 0.1));
  --pill-bg-1: color-mix(in oklch, var(--cf-ink) 5%, transparent);
  --pill-bg-2: color-mix(in oklch, var(--cf-ink) 2%, transparent);
  --pill-shadow: color-mix(in oklch, var(--cf-ink) 14%, transparent);
  --pill-highlight: color-mix(in oklch, var(--cf-bg) 70%, transparent);
  --pill-border: color-mix(in oklch, var(--cf-ink) 9%, transparent);
  --pill-bg-1-hover: color-mix(in oklch, var(--cf-ink) 10%, transparent);
  --pill-bg-2-hover: color-mix(in oklch, var(--cf-ink) 4%, transparent);
  --pill-border-hover: color-mix(in oklch, var(--cf-ink) 22%, transparent);
  background: var(--cf-bg);
  color: var(--cf-ink);
  font-family: var(--aura-font, system-ui, sans-serif);
  -webkit-font-smoothing: antialiased;
}

@keyframes footer-breathe {
  0% { transform: translate(-50%, -50%) scale(1); opacity: 0.6; }
  100% { transform: translate(-50%, -50%) scale(1.1); opacity: 1; }
}
@keyframes footer-scroll-marquee {
  from { transform: translateX(0); }
  to { transform: translateX(-50%); }
}
.animate-footer-breathe { animation: footer-breathe 8s ease-in-out infinite alternate; }
.animate-footer-scroll-marquee { animation: footer-scroll-marquee 40s linear infinite; }

.footer-bg-grid {
  background-size: 60px 60px;
  background-image:
    linear-gradient(to right, color-mix(in oklch, var(--cf-ink) 4%, transparent) 1px, transparent 1px),
    linear-gradient(to bottom, color-mix(in oklch, var(--cf-ink) 4%, transparent) 1px, transparent 1px);
  mask-image: linear-gradient(to bottom, transparent, black 30%, black 70%, transparent);
  -webkit-mask-image: linear-gradient(to bottom, transparent, black 30%, black 70%, transparent);
}

/* 무채색 크롬 빛 (Aura는 강조색이 없음) */
.footer-aurora {
  background: radial-gradient(
    circle at 50% 50%,
    color-mix(in oklch, var(--cf-ink) 14%, transparent) 0%,
    color-mix(in oklch, var(--cf-muted) 10%, transparent) 40%,
    transparent 70%
  );
}

.footer-marquee {
  border-block: 1px solid var(--cf-line);
  background: color-mix(in oklch, var(--cf-bg) 60%, transparent);
  -webkit-backdrop-filter: blur(12px);
  backdrop-filter: blur(12px);
  color: var(--cf-muted);
  font-family: var(--aura-mono, ui-monospace, monospace);
}

.footer-glass-pill {
  background: linear-gradient(145deg, var(--pill-bg-1) 0%, var(--pill-bg-2) 100%);
  box-shadow:
    0 10px 30px -10px var(--pill-shadow),
    inset 0 1px 1px var(--pill-highlight);
  border: 1px solid var(--pill-border);
  -webkit-backdrop-filter: blur(16px);
  backdrop-filter: blur(16px);
  color: var(--cf-ink);
  transition: background 0.4s cubic-bezier(0.16, 1, 0.3, 1), border-color 0.4s, box-shadow 0.4s, color 0.4s;
}
.footer-glass-pill:hover {
  background: linear-gradient(145deg, var(--pill-bg-1-hover) 0%, var(--pill-bg-2-hover) 100%);
  border-color: var(--pill-border-hover);
}
.footer-glass-pill.is-muted { color: var(--cf-muted); }
.footer-glass-pill.is-muted:hover { color: var(--cf-ink); }
/* 주 버튼: 로그인 화면의 검정 알약과 같은 모양 */
.footer-glass-pill.is-primary {
  background: var(--aura-button, #0b0b0c);
  color: var(--aura-button-ink, #fff);
  border-color: transparent;
  box-shadow: 0 18px 30px -18px rgba(0, 0, 0, 0.55);
}
.footer-glass-pill.is-primary:hover { background: var(--aura-button, #0b0b0c); }
.footer-glass-pill:focus-visible {
  outline: 2px solid var(--aura-ring, rgba(11, 11, 12, 0.22));
  outline-offset: 3px;
}

.footer-giant-bg-text {
  font-size: 22vw;
  line-height: 0.75;
  font-weight: 700;
  letter-spacing: -0.05em;
  color: transparent;
  -webkit-text-stroke: 1px color-mix(in oklch, var(--cf-ink) 7%, transparent);
  background: linear-gradient(180deg, color-mix(in oklch, var(--cf-ink) 12%, transparent) 0%, transparent 60%);
  -webkit-background-clip: text;
  background-clip: text;
}

.footer-text-glow {
  background: linear-gradient(180deg, var(--cf-ink) 0%, color-mix(in oklch, var(--cf-ink) 45%, transparent) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 0 20px color-mix(in oklch, var(--cf-ink) 12%, transparent));
}
.footer-muted { color: var(--cf-muted); }
/* 한 화면으로 쓸 때: 뒤의 배경(3D 크롬 등)이 그대로 보이게 */
.cinematic-footer.is-page { background: transparent; }

@media (prefers-reduced-motion: reduce) {
  .animate-footer-breathe, .animate-footer-scroll-marquee { animation: none; }
}
`

const reducedMotion = () =>
  typeof matchMedia === "function" && matchMedia("(prefers-reduced-motion: reduce)").matches

// ───────── 마우스를 따라 살짝 끌려오는 버튼 ─────────
export type MagneticButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> &
  React.AnchorHTMLAttributes<HTMLAnchorElement> & {
    as?: React.ElementType
  }

export const MagneticButton = React.forwardRef<HTMLElement, MagneticButtonProps>(
  ({ className, children, as: Component = "button", ...props }, forwardedRef) => {
    const localRef = useRef<HTMLElement | null>(null)

    useEffect(() => {
      const element = localRef.current
      if (!element || reducedMotion()) return

      const handleMouseMove = (e: MouseEvent) => {
        const rect = element.getBoundingClientRect()
        const x = e.clientX - rect.left - rect.width / 2
        const y = e.clientY - rect.top - rect.height / 2
        gsap.to(element, {
          x: x * 0.4,
          y: y * 0.4,
          rotationX: -y * 0.15,
          rotationY: x * 0.15,
          scale: 1.05,
          ease: "power2.out",
          duration: 0.4,
        })
      }
      const handleMouseLeave = () => {
        gsap.to(element, { x: 0, y: 0, rotationX: 0, rotationY: 0, scale: 1, ease: "elastic.out(1, 0.3)", duration: 1.2 })
      }

      element.addEventListener("mousemove", handleMouseMove)
      element.addEventListener("mouseleave", handleMouseLeave)
      return () => {
        element.removeEventListener("mousemove", handleMouseMove)
        element.removeEventListener("mouseleave", handleMouseLeave)
        gsap.killTweensOf(element)
      }
    }, [])

    // a/button 어느 쪽이든 그릴 수 있게 (태그 이름을 그대로 받음)
    const Tag = Component as React.FC<Record<string, unknown>>
    return (
      <Tag
        ref={(node: HTMLElement | null) => {
          localRef.current = node
          if (typeof forwardedRef === "function") forwardedRef(node)
          else if (forwardedRef) forwardedRef.current = node
        }}
        className={cn("cursor-pointer", className)}
        {...props}
      >
        {children}
      </Tag>
    )
  }
)
MagneticButton.displayName = "MagneticButton"

// ───────── 푸터 ─────────
export type FooterLink = {
  label: string
  href?: string
  onClick?: () => void // 주소 대신 동작이 필요하면 (버튼으로 그림)
  icon?: React.ReactNode
  primary?: boolean
}

const linkProps = (l: FooterLink) =>
  l.onClick ? { as: "button" as const, type: "button" as const, onClick: l.onClick } : { as: "a" as const, href: l.href }

export type CinematicFooterProps = {
  heading: React.ReactNode
  giantText: string
  marquee: string[]
  actions: FooterLink[] // 큰 알약 버튼
  links?: FooterLink[] // 작은 보조 링크
  copyright: string
  credit?: React.ReactNode
  // curtain: 본문 아래에서 커튼처럼 드러나는 푸터 (원본 동작)
  // page: 이것만으로 한 화면 (배경은 투명, 처음 열릴 때 떠오름)
  mode?: "curtain" | "page"
  corner?: React.ReactNode // page 모드에서 오른쪽 아래 (없으면 맨 위로 버튼)
}

function Marquee({ items }: { items: string[] }) {
  return (
    <div className="flex items-center gap-12 px-6">
      {items.map((item) => (
        <React.Fragment key={item}>
          <span>{item}</span>
          <span aria-hidden>✦</span>
        </React.Fragment>
      ))}
    </div>
  )
}

export function CinematicFooter({
  heading,
  giantText,
  marquee,
  actions,
  links = [],
  copyright,
  credit,
  mode = "curtain",
  corner,
}: CinematicFooterProps) {
  const isPage = mode === "page"
  const rootRef = useRef<HTMLDivElement>(null)
  const giantTextRef = useRef<HTMLDivElement>(null)
  const headingRef = useRef<HTMLHeadingElement>(null)
  const linksRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!rootRef.current || reducedMotion()) return

    const ctx = gsap.context(() => {
      if (isPage) {
        // 처음 열릴 때: 큰 글자가 올라오고, 제목·버튼이 차례로 떠오름
        gsap.fromTo(giantTextRef.current, { y: "10vh", scale: 0.85, opacity: 0 }, { y: 0, scale: 1, opacity: 1, duration: 1.6, ease: "power3.out" })
        gsap.fromTo(
          [headingRef.current, linksRef.current],
          { y: 40, opacity: 0 },
          { y: 0, opacity: 1, duration: 1, stagger: 0.15, delay: 0.2, ease: "power3.out" }
        )
        return
      }
      gsap.fromTo(
        giantTextRef.current,
        { y: "10vh", scale: 0.8, opacity: 0 },
        {
          y: "0vh",
          scale: 1,
          opacity: 1,
          ease: "power1.out",
          scrollTrigger: { trigger: rootRef.current, start: "top 80%", end: "bottom bottom", scrub: 1 },
        }
      )
      gsap.fromTo(
        [headingRef.current, linksRef.current],
        { y: 50, opacity: 0 },
        {
          y: 0,
          opacity: 1,
          stagger: 0.15,
          ease: "power3.out",
          scrollTrigger: { trigger: rootRef.current, start: "top 40%", end: "bottom bottom", scrub: 1 },
        }
      )
    }, rootRef)

    return () => ctx.revert()
  }, [isPage])

  const scrollToTop = () => window.scrollTo({ top: 0, behavior: reducedMotion() ? "auto" : "smooth" })

  const content = (
    <>
      {!isPage && (
        <>
          <div className="footer-aurora animate-footer-breathe pointer-events-none absolute left-1/2 top-1/2 z-0 h-[60vh] w-[80vw] -translate-x-1/2 -translate-y-1/2 rounded-[50%] blur-[80px]" />
          <div className="footer-bg-grid pointer-events-none absolute inset-0 z-0" />
        </>
      )}

      <div
        ref={giantTextRef}
        aria-hidden
        className="footer-giant-bg-text pointer-events-none absolute -bottom-[5vh] left-1/2 z-0 -translate-x-1/2 whitespace-nowrap select-none"
      >
        {giantText}
      </div>

      <div className="footer-marquee absolute left-0 top-12 z-10 w-full -rotate-2 scale-110 overflow-hidden py-4 shadow-2xl" aria-hidden>
        <div className="animate-footer-scroll-marquee flex w-max text-xs font-medium tracking-[0.14em] uppercase md:text-sm">
          <Marquee items={marquee} />
          <Marquee items={marquee} />
        </div>
      </div>

      <div className="relative z-10 mx-auto mt-20 flex w-full max-w-5xl flex-1 flex-col items-center justify-center px-6">
        {isPage ? (
          <h1 ref={headingRef} className="footer-text-glow mb-12 text-center text-5xl font-bold tracking-tighter md:text-7xl">
            {heading}
          </h1>
        ) : (
          <h2 ref={headingRef} className="footer-text-glow mb-12 text-center text-5xl font-bold tracking-tighter md:text-7xl">
            {heading}
          </h2>
        )}

        <div ref={linksRef} className="flex w-full flex-col items-center gap-6">
          <div className="flex w-full flex-wrap justify-center gap-4">
            {actions.map((a) => (
              <MagneticButton
                key={a.label}
                {...linkProps(a)}
                className={cn(
                  "footer-glass-pill group flex items-center gap-3 rounded-full px-10 py-5 text-sm font-bold md:text-base",
                  a.primary && "is-primary"
                )}
              >
                {a.label}
                {a.icon}
              </MagneticButton>
            ))}
          </div>

          {links.length > 0 && (
            <div className="mt-2 flex w-full flex-wrap justify-center gap-3 md:gap-6">
              {links.map((l) => (
                <MagneticButton
                  key={l.label}
                  {...linkProps(l)}
                  className="footer-glass-pill is-muted rounded-full px-6 py-3 text-xs font-medium md:text-sm"
                >
                  {l.label}
                </MagneticButton>
              ))}
            </div>
          )}
        </div>
      </div>

      <div className="relative z-20 flex w-full flex-col items-center justify-between gap-6 px-6 pb-8 md:flex-row md:px-12">
        <div className="footer-muted order-2 text-[10px] font-semibold tracking-widest uppercase md:order-1 md:text-xs">
          {copyright}
        </div>

        {credit && (
          <div className="footer-glass-pill is-muted order-1 flex cursor-default items-center gap-2 rounded-full px-6 py-3 text-[10px] font-semibold tracking-widest md:order-2 md:text-xs">
            {credit}
          </div>
        )}

        <div className="order-3">
          {isPage && corner ? (
            corner
          ) : (
            <MagneticButton
              as="button"
              type="button"
              onClick={scrollToTop}
              aria-label="맨 위로"
              className="footer-glass-pill is-muted group flex h-12 w-12 items-center justify-center rounded-full"
            >
              <ArrowUp className="h-5 w-5 transition-transform duration-300 group-hover:-translate-y-1.5" aria-hidden />
            </MagneticButton>
          )}
        </div>
      </div>
    </>
  )

  return (
    <>
      <style dangerouslySetInnerHTML={{ __html: STYLES }} />

      {isPage ? (
        <div ref={rootRef} className="cinematic-footer is-page relative flex h-full w-full flex-col justify-between overflow-hidden">
          {content}
        </div>
      ) : (
        <div ref={rootRef} className="relative h-screen w-full" style={{ clipPath: "polygon(0% 0, 100% 0%, 100% 100%, 0 100%)" }}>
          <footer className="cinematic-footer fixed bottom-0 left-0 flex h-screen w-full flex-col justify-between overflow-hidden">
            {content}
          </footer>
        </div>
      )}
    </>
  )
}
