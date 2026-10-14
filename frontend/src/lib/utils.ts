import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

// shadcn 컴포넌트가 쓰는 클래스 합치기 도우미 (겹치는 Tailwind 클래스는 뒤의 것이 이김)
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
