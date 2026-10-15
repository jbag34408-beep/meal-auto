// 식단 화면에서 쓰는 Spring Boot API (응답 모양은 서버의 DTO·엔티티와 같음)

export type MealType = "BREAKFAST" | "LUNCH" | "DINNER" | "SNACK"

export const MEAL_ORDER: MealType[] = ["BREAKFAST", "LUNCH", "DINNER", "SNACK"]
export const MEAL_NAMES: Record<MealType, string> = {
  BREAKFAST: "아침",
  LUNCH: "점심",
  DINNER: "저녁",
  SNACK: "간식",
}

export type Me = {
  username: string
  targetKcal: number
  targetCarbohydrate: number
  targetProtein: number
  targetFat: number
}

export type Food = {
  id: number
  name: string
  kcalPer100g: number
  carbohydrate: number
  protein: number
  fat: number
  source?: "SEED" | "PUBLIC" | "USER" | "AI" | string
  shared?: boolean // true: 모두가 쓰는 공용 음식, false: 내 개인 음식
}

export type MealEntry = { id: number; date: string; mealType: MealType; food: Food; grams: number }

export type Summary = {
  date: string
  mealCount: number
  totalKcal: number
  totalCarbohydrate: number
  totalProtein: number
  totalFat: number
}

export type Shortfall = { nutrient: string; target: number; eaten: number; lack: number; ratio: number }

export type Recommendation = {
  food: Food
  grams: number
  reason: string
  nutrient: string
  neededGrams: number
  kcal: number
  remainingKcal: number
  capped: boolean
  shortfalls: Shortfall[]
}

export type PublicFood = {
  name: string
  maker?: string
  basis?: string
  kcal: number
  carbohydrate: number
  protein: number
  fat: number
}

export type FoodInput = Omit<Food, "id" | "source">
export type Targets = { kcal: number; carbohydrate: number; protein: number; fat: number }

// 서버가 돌려준 오류 상태를 그대로 들고 다님 (AI 503 처리에 필요)
export class ApiError extends Error {
  constructor(message: string, readonly status: number) {
    super(message)
  }
}

// 모든 요청은 여기로: 로그인이 풀렸으면(401) 로그인 화면으로
async function request(url: string, init?: RequestInit) {
  const res = await fetch(url, init)
  if (res.status === 401) {
    location.href = "/login.html"
    throw new ApiError("로그인이 필요해요", 401)
  }
  if (!res.ok) throw new ApiError(await errorText(res), res.status)
  return res
}

async function errorText(res: Response) {
  try {
    const err = (await res.json()) as { message?: string; errors?: Record<string, string> }
    return Object.values(err.errors ?? {}).join(", ") || err.message || `요청 실패 (${res.status})`
  } catch {
    return `요청 실패 (${res.status})`
  }
}

const json = (body: unknown, method = "POST"): RequestInit => ({
  method,
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify(body),
})

export const getMe = async (): Promise<Me> => (await request("/me")).json()
export const getFoods = async (): Promise<Food[]> => (await request("/foods")).json()
export const getMeals = async (date: string): Promise<MealEntry[]> =>
  (await request(`/meals?date=${date}`)).json()
export const getSummary = async (date: string): Promise<Summary> =>
  (await request(`/summary?date=${date}`)).json()

// 추천할 음식이 없으면 서버가 빈 본문을 보냄
export async function getRecommendation(date: string): Promise<Recommendation | null> {
  const text = await (await request(`/recommendation?date=${date}`)).text()
  return text ? (JSON.parse(text) as Recommendation) : null
}

export async function addMeal(entry: { date: string; mealType: MealType; foodId: number; grams: number }) {
  await request("/meals", json(entry))
}

export const analyzeMeal = async (body: { date: string; mealType: MealType; text: string }): Promise<MealEntry[]> =>
  (await request("/meals/analyze", json(body))).json()

export async function deleteMeal(id: number) {
  await request(`/meals/${id}`, { method: "DELETE" })
}

export async function saveTargets(targets: Targets) {
  await request("/me/targets", json(targets, "PUT"))
}

// 직접 등록 → 내 개인 음식
export const createFood = async (food: FoodInput): Promise<Food> => (await request("/foods", json(food))).json()

// 공공데이터 가져오기 → 공용 음식 (영양 값은 서버가 식약처 DB를 다시 조회해서 채움)
export const importPublicFood = async (food: Pick<PublicFood, "name" | "maker">): Promise<Food> =>
  (await request("/foods/public", json({ name: food.name, maker: food.maker ?? "" }))).json()

export const searchPublicFoods = async (keyword: string): Promise<PublicFood[]> =>
  (await request(`/foods/public?keyword=${encodeURIComponent(keyword)}`)).json()

export async function logout() {
  await fetch("/logout", { method: "POST" })
  location.href = "/login.html"
}

// ── 날짜·숫자 도우미 ──

export const fmt = (n: number) => Number(n).toLocaleString("ko-KR", { maximumFractionDigits: 1 })

// 한국 시각 기준 오늘 (YYYY-MM-DD)
export const todayKst = () => new Date(Date.now() + 9 * 3600 * 1000).toISOString().slice(0, 10)

export function shiftDate(date: string, days: number) {
  const d = new Date(`${date}T00:00:00Z`)
  d.setUTCDate(d.getUTCDate() + days)
  return d.toISOString().slice(0, 10)
}

// 지금 시각(한국)에 맞는 끼니
export function currentMealType(): MealType {
  const hour = new Date(Date.now() + 9 * 3600 * 1000).getUTCHours()
  if (hour < 10) return "BREAKFAST"
  if (hour < 15) return "LUNCH"
  if (hour < 21) return "DINNER"
  return "SNACK"
}

export const mealKcal = (m: MealEntry) => Math.round((m.food.kcalPer100g * m.grams) / 100)
