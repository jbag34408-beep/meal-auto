import { useState, type FormEvent, type MouseEvent, type ReactNode } from "react"
import { motion, AnimatePresence, MotionConfig, useMotionValue, useTransform, type Transition } from "framer-motion"
import { User, Lock, Eye, EyeClosed, ArrowRight, Sparkles } from "lucide-react"
import { Input } from "@/components/ui/input"
import { cn } from "@/lib/utils"

/**
 * 로그인 카드 — 21st.dev "sign-in-card-2"를 바탕으로 만듦
 * 원본의 보라색 빛 대신 하얀 반투명 유리 구조로 바꾸고, 우리 서버 기능(아이디 로그인·회원가입·체험 계정)에 맞춤
 */

export type SignInMode = "login" | "signup"

export interface SignInCardProps {
  mode: SignInMode
  onModeChange: (mode: SignInMode) => void
  onSubmit: (username: string, password: string) => void
  onDemo: () => void
  busy: boolean
  error: string
}

const COPY = {
  login: { title: "다시 오셨네요", sub: "식단 자동화에 로그인하세요", submit: "로그인", switchText: "아직 계정이 없나요?", switchAction: "회원가입" },
  signup: { title: "처음 오셨군요", sub: "아이디와 비밀번호만 있으면 시작할 수 있어요", submit: "가입하고 시작하기", switchText: "이미 계정이 있나요?", switchAction: "로그인" },
} as const

// 테두리를 따라 도는 빛줄기 (흰 화면에서도 보이도록 회색 계열)
function Beam({ side, delay }: { side: "top" | "right" | "bottom" | "left"; delay: number }) {
  const horizontal = side === "top" || side === "bottom"
  const prop = side === "top" ? "left" : side === "right" ? "top" : side === "bottom" ? "right" : "bottom"
  return (
    <motion.div
      className={cn(
        "absolute opacity-70",
        horizontal ? "h-[2px] w-1/2 bg-gradient-to-r" : "h-1/2 w-[2px] bg-gradient-to-b",
        "from-transparent via-neutral-500/70 to-transparent",
        side === "top" && "top-0 left-0",
        side === "right" && "top-0 right-0",
        side === "bottom" && "bottom-0 right-0",
        side === "left" && "bottom-0 left-0"
      )}
      initial={{ filter: "blur(1px)" }}
      animate={{ [prop]: ["-50%", "100%"], opacity: [0.25, 0.7, 0.25], filter: ["blur(0.5px)", "blur(1.5px)", "blur(0.5px)"] }}
      transition={
        {
          [prop]: { duration: 2.5, ease: "easeInOut", repeat: Infinity, repeatDelay: 1, delay },
          opacity: { duration: 1.2, repeat: Infinity, repeatType: "mirror", delay },
          filter: { duration: 1.5, repeat: Infinity, repeatType: "mirror", delay },
        } as Transition
      }
    />
  )
}

// 유리 입력칸 (아이콘 + 포커스 강조)
function GlassField({
  icon,
  focused,
  children,
}: {
  icon: ReactNode
  focused: boolean
  children: ReactNode
}) {
  return (
    <motion.div
      className={cn("relative", focused && "z-10")}
      whileHover={{ scale: 1.01 }}
      transition={{ type: "spring", stiffness: 400, damping: 25 }}
    >
      <div className="relative flex items-center overflow-hidden rounded-xl">
        <span className={cn("absolute left-3.5 transition-colors duration-300", focused ? "text-neutral-900" : "text-neutral-400")}>
          {icon}
        </span>
        {children}
        {focused && (
          <motion.div
            layoutId="input-highlight"
            className="absolute inset-0 -z-10 bg-white/60"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
          />
        )}
      </div>
    </motion.div>
  )
}

const fieldClass =
  "h-12 w-full rounded-xl border-black/[0.06] bg-white/45 pl-11 text-[15px] text-neutral-900 placeholder:text-neutral-400 " +
  "shadow-[inset_0_1px_2px_rgba(0,0,0,0.04)] focus:border-black/10 focus:bg-white/80"

export function SignInCard({ mode, onModeChange, onSubmit, onDemo, busy, error }: SignInCardProps) {
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [showPassword, setShowPassword] = useState(false)
  const [focused, setFocused] = useState<"username" | "password" | null>(null)
  const copy = COPY[mode]

  // 마우스를 따라 카드가 기울어지는 3D 효과
  const mouseX = useMotionValue(0)
  const mouseY = useMotionValue(0)
  const rotateX = useTransform(mouseY, [-300, 300], [10, -10])
  const rotateY = useTransform(mouseX, [-300, 300], [-10, 10])

  function handleMouseMove(e: MouseEvent<HTMLDivElement>) {
    const rect = e.currentTarget.getBoundingClientRect()
    mouseX.set(e.clientX - rect.left - rect.width / 2)
    mouseY.set(e.clientY - rect.top - rect.height / 2)
  }
  function handleMouseLeave() {
    mouseX.set(0)
    mouseY.set(0)
  }

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    onSubmit(username, password)
  }

  function switchMode() {
    setPassword("")
    onModeChange(mode === "login" ? "signup" : "login")
  }

  return (
    // 사용자가 '동작 줄이기'를 켜 두면 움직임을 끔
    <MotionConfig reducedMotion="user">
      <div className="relative flex min-h-svh w-full items-center justify-center overflow-hidden bg-gradient-to-b from-white via-[#f3f4f2] to-[#e7eae6] px-4 py-10">
        {/* 바탕: 흰빛·회색 빛 덩어리가 유리 뒤에서 은은하게 번짐 */}
        <div className="absolute top-0 left-1/2 h-[60vh] w-[120vh] -translate-x-1/2 rounded-b-[50%] bg-white blur-[80px]" />
        <motion.div
          className="absolute top-0 left-1/2 h-[60vh] w-[100vh] -translate-x-1/2 rounded-b-full bg-neutral-300/40 blur-[70px]"
          animate={{ opacity: [0.35, 0.6, 0.35], scale: [0.98, 1.02, 0.98] }}
          transition={{ duration: 8, repeat: Infinity, repeatType: "mirror" }}
        />
        <motion.div
          className="absolute bottom-0 left-1/2 h-[90vh] w-[90vh] -translate-x-1/2 rounded-t-full bg-[#d7ddd6]/60 blur-[70px]"
          animate={{ opacity: [0.4, 0.7, 0.4], scale: [1, 1.1, 1] }}
          transition={{ duration: 6, repeat: Infinity, repeatType: "mirror", delay: 1 }}
        />
        <div className="absolute top-1/4 left-1/4 h-96 w-96 animate-pulse rounded-full bg-white/80 blur-[100px] motion-reduce:animate-none" />
        <div className="absolute right-1/4 bottom-1/4 h-96 w-96 animate-pulse rounded-full bg-neutral-200/70 blur-[100px] [animation-delay:1s] motion-reduce:animate-none" />
        {/* 아주 옅은 노이즈 */}
        <div
          className="absolute inset-0 opacity-[0.035] mix-blend-multiply"
          style={{
            backgroundImage: `url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E")`,
            backgroundSize: "200px 200px",
          }}
        />

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
          className="relative z-10 w-full max-w-sm"
          style={{ perspective: 1500 }}
        >
          <motion.div
            className="relative"
            style={{ rotateX, rotateY }}
            onMouseMove={handleMouseMove}
            onMouseLeave={handleMouseLeave}
            whileHover={{ z: 10 }}
          >
            <div className="group relative">
              {/* 테두리 빛줄기와 모서리 빛 */}
              <div className="absolute -inset-px overflow-hidden rounded-[22px]">
                <Beam side="top" delay={0} />
                <Beam side="right" delay={0.6} />
                <Beam side="bottom" delay={1.2} />
                <Beam side="left" delay={1.8} />
              </div>

              {/* 유리 카드 */}
              <div
                className={cn(
                  "relative overflow-hidden rounded-[22px] border border-white/80 bg-white/40 p-7 backdrop-blur-2xl",
                  "shadow-[0_30px_60px_-30px_rgba(15,20,17,0.35),inset_0_1px_0_rgba(255,255,255,0.9),inset_0_0_0_1px_rgba(255,255,255,0.4)]"
                )}
              >
                {/* 카드 안쪽의 옅은 격자 무늬 */}
                <div
                  className="pointer-events-none absolute inset-0 opacity-[0.035]"
                  style={{
                    backgroundImage: "linear-gradient(135deg, black 0.5px, transparent 0.5px), linear-gradient(45deg, black 0.5px, transparent 0.5px)",
                    backgroundSize: "30px 30px",
                  }}
                />
                {/* 위쪽에서 들어오는 빛 */}
                <div className="pointer-events-none absolute inset-x-0 top-0 h-1/3 bg-gradient-to-b from-white/70 to-transparent" />

                {/* 로고와 제목 */}
                <div className="relative mb-6 space-y-1.5 text-center">
                  <motion.div
                    initial={{ scale: 0.5, opacity: 0 }}
                    animate={{ scale: 1, opacity: 1 }}
                    transition={{ type: "spring", duration: 0.8 }}
                    className="liquid-glass relative mx-auto mb-3 grid h-12 w-12 place-items-center rounded-full"
                    aria-hidden="true"
                  >
                    {/* 세 가닥 표식: 탄수화물·단백질·지방 */}
                    <span className="grid w-5 gap-[3px]">
                      <i className="block h-[3px] w-full rounded-full bg-carb" />
                      <i className="block h-[3px] w-[70%] rounded-full bg-protein" />
                      <i className="block h-[3px] w-[45%] rounded-full bg-fat" />
                    </span>
                  </motion.div>
                  <AnimatePresence mode="wait">
                    <motion.div
                      key={mode}
                      initial={{ opacity: 0, y: 8 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, y: -6 }}
                      transition={{ duration: 0.25 }}
                    >
                      <h1 className="text-[22px] font-bold tracking-[-0.02em] text-neutral-900">{copy.title}</h1>
                      <p className="mt-1 text-[13px] text-neutral-500">{copy.sub}</p>
                    </motion.div>
                  </AnimatePresence>
                </div>

                {/* 폼 */}
                <form onSubmit={handleSubmit} className="relative space-y-4">
                  <div className="space-y-3">
                    <GlassField icon={<User className="h-4 w-4" />} focused={focused === "username"}>
                      <Input
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        onFocus={() => setFocused("username")}
                        onBlur={() => setFocused(null)}
                        placeholder={mode === "login" ? "아이디" : "아이디 (영문·숫자 4~20자)"}
                        aria-label="아이디"
                        autoComplete="username"
                        required
                        className={cn(fieldClass, "pr-3")}
                      />
                    </GlassField>

                    <GlassField icon={<Lock className="h-4 w-4" />} focused={focused === "password"}>
                      <Input
                        type={showPassword ? "text" : "password"}
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        onFocus={() => setFocused("password")}
                        onBlur={() => setFocused(null)}
                        placeholder={mode === "login" ? "비밀번호" : "비밀번호 (8자 이상)"}
                        aria-label="비밀번호"
                        autoComplete={mode === "login" ? "current-password" : "new-password"}
                        required
                        className={cn(fieldClass, "pr-11")}
                      />
                      <button
                        type="button"
                        onClick={() => setShowPassword((v) => !v)}
                        aria-label={showPassword ? "비밀번호 숨기기" : "비밀번호 보기"}
                        className="absolute right-3 rounded-md p-1 text-neutral-400 transition-colors hover:text-neutral-900 focus-visible:outline-2 focus-visible:outline-neutral-900"
                      >
                        {showPassword ? <Eye className="h-4 w-4" /> : <EyeClosed className="h-4 w-4" />}
                      </button>
                    </GlassField>
                  </div>

                  {/* 주 버튼: 원본 코드의 동작(빛 번짐, 로딩 스피너·반짝임, 화살표)을 유리 질감으로 */}
                  <motion.button
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    type="submit"
                    disabled={busy}
                    className="group/button relative mt-2 block w-full rounded-full focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-neutral-900 disabled:cursor-default"
                  >
                    <div className="absolute inset-0 rounded-full bg-white blur-lg opacity-0 transition-opacity duration-300 group-hover/button:opacity-90" />
                    <div className="liquid-glass relative flex h-12 items-center justify-center overflow-hidden rounded-full font-semibold text-[#151515]">
                      <motion.div
                        className="absolute inset-0 bg-gradient-to-r from-white/0 via-white/80 to-white/0"
                        animate={{ x: ["-100%", "100%"] }}
                        transition={{ duration: 1.5, ease: "easeInOut", repeat: Infinity, repeatDelay: 1 }}
                        style={{ opacity: busy ? 1 : 0, transition: "opacity 0.3s ease" }}
                      />
                      <AnimatePresence mode="wait">
                        {busy ? (
                          <motion.div key="loading" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
                            <div className="h-4 w-4 animate-spin rounded-full border-2 border-black/70 border-t-transparent" />
                            <span className="sr-only">확인 중</span>
                          </motion.div>
                        ) : (
                          <motion.span
                            key={copy.submit}
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            exit={{ opacity: 0 }}
                            className="relative flex items-center gap-1.5 text-[15px]"
                          >
                            {copy.submit}
                            <ArrowRight className="h-3.5 w-3.5 transition-transform duration-300 group-hover/button:translate-x-1" />
                          </motion.span>
                        )}
                      </AnimatePresence>
                    </div>
                  </motion.button>

                  <p className="min-h-5 text-center text-[13px] text-danger" role="alert">
                    {error}
                  </p>

                  {/* 구분선 */}
                  <div className="relative flex items-center">
                    <div className="flex-grow border-t border-black/[0.07]" />
                    <span className="mx-3 text-xs text-neutral-400">또는</span>
                    <div className="flex-grow border-t border-black/[0.07]" />
                  </div>

                  {/* 보조 버튼: 원본의 Google 로그인 자리 → 체험 계정 */}
                  <motion.button
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    type="button"
                    onClick={onDemo}
                    disabled={busy}
                    className="group/demo relative block w-full rounded-full focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-neutral-900 disabled:opacity-50"
                  >
                    <div className="absolute inset-0 rounded-full bg-white/70 blur opacity-0 transition-opacity duration-300 group-hover/demo:opacity-90" />
                    <div className="relative flex h-11 items-center justify-center gap-2 overflow-hidden rounded-full border border-white/80 bg-white/35 text-neutral-700 shadow-[inset_0_1px_0_rgba(255,255,255,0.9),0_8px_20px_-14px_rgba(0,0,0,0.3)] backdrop-blur-md transition-colors duration-300 group-hover/demo:text-neutral-900">
                      <Sparkles className="h-4 w-4" />
                      <span className="text-[13px] font-medium">체험 계정으로 들어가기 (demo / demo1234)</span>
                      <motion.div
                        className="absolute inset-0 bg-gradient-to-r from-white/0 via-white/60 to-white/0"
                        initial={{ x: "-100%" }}
                        whileHover={{ x: "100%" }}
                        transition={{ duration: 1, ease: "easeInOut" }}
                      />
                    </div>
                  </motion.button>

                  {/* 로그인 ↔ 회원가입 전환 */}
                  <p className="pt-1 text-center text-[13px] text-neutral-500">
                    {copy.switchText}{" "}
                    <button
                      type="button"
                      onClick={switchMode}
                      className="group/switch relative inline-block font-semibold text-neutral-900 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-neutral-900"
                    >
                      {copy.switchAction}
                      <span className="absolute bottom-0 left-0 h-px w-0 bg-neutral-900 transition-all duration-300 group-hover/switch:w-full" />
                    </button>
                  </p>
                </form>
              </div>
            </div>
          </motion.div>

          <p className="mt-6 text-center text-[13px] text-neutral-500">
            <a href="/landing.html" className="underline underline-offset-4 hover:text-neutral-900">
              식단 자동화는 어떻게 동작하나요?
            </a>
          </p>
        </motion.div>
      </div>
    </MotionConfig>
  )
}
