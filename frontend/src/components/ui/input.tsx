import * as React from "react"
import { cn } from "@/lib/utils"

// shadcn Input (sign-in-card-2에 들어 있던 것을 공용 컴포넌트로 분리)
function Input({ className, type, ...props }: React.ComponentProps<"input">) {
  return (
    <input
      type={type}
      data-slot="input"
      className={cn(
        "flex h-9 w-full min-w-0 rounded-md border bg-transparent px-3 py-1 text-base outline-none",
        "transition-[color,box-shadow,background-color,border-color] disabled:pointer-events-none disabled:opacity-50",
        "focus-visible:ring-[3px] focus-visible:ring-white/20",
        className
      )}
      {...props}
    />
  )
}

export { Input }
