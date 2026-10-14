import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "@/lib/utils"

/**
 * Liquid Glass 버튼
 * - 알약 모양의 반투명 흰 유리: 뒤 배경이 흐리게 비침
 * - 위쪽 안쪽으로 번지는 그늘 → 볼록한 유리 두께감
 * - 아래쪽과 양 끝에만 보이는 얇고 짙은 굴절선
 * - 아래로 넓게 퍼지는 부드러운 그림자
 * 그림자 조합은 index.css의 .liquid-glass 에 있음 (Tailwind 한 줄로 쓰기엔 길어서)
 */
const liquidGlassButtonVariants = cva(
  [
    "liquid-glass relative inline-flex items-center justify-center gap-2 rounded-full",
    "font-semibold tracking-[-0.01em] text-[#151515] whitespace-nowrap select-none",
    "transition-[transform,box-shadow] duration-200 ease-[cubic-bezier(0.16,1,0.3,1)]",
    "hover:-translate-y-px active:translate-y-0 active:scale-[0.97]",
    "focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-[#151515]",
    "disabled:pointer-events-none disabled:opacity-50",
    "motion-reduce:transition-none motion-reduce:hover:translate-y-0 motion-reduce:active:scale-100",
  ],
  {
    variants: {
      size: {
        sm: "h-9 px-4 text-[13px]",
        md: "h-12 px-6 text-[15px]",
        lg: "h-14 px-8 text-[17px]",
      },
      block: {
        true: "w-full",
        false: "",
      },
    },
    defaultVariants: { size: "md", block: false },
  }
)

export interface LiquidGlassButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof liquidGlassButtonVariants> {
  /** true면 자식 요소(예: <a>)에 버튼 모양만 입힘 */
  asChild?: boolean
}

const LiquidGlassButton = React.forwardRef<HTMLButtonElement, LiquidGlassButtonProps>(
  ({ className, size, block, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : "button"
    return <Comp ref={ref} className={cn(liquidGlassButtonVariants({ size, block }), className)} {...props} />
  }
)
LiquidGlassButton.displayName = "LiquidGlassButton"

export { LiquidGlassButton, liquidGlassButtonVariants }
