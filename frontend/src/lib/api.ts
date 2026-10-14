// Spring Boot API 호출 모음 (같은 주소라서 로그인 세션 쿠키가 자동으로 붙음)

type ErrorBody = { message?: string; errors?: Record<string, string> }

// 서버 오류 응답에서 사용자에게 보여줄 문장을 꺼냄
async function errorText(res: Response, fallback: string) {
  try {
    const body = (await res.json()) as ErrorBody
    return Object.values(body.errors ?? {}).join(", ") || body.message || fallback
  } catch {
    return fallback
  }
}

// Spring Security 로그인은 JSON이 아니라 폼 형식(username=...&password=...)으로 보냄
export async function login(username: string, password: string) {
  const res = await fetch("/login", {
    method: "POST",
    body: new URLSearchParams({ username, password }),
  })
  if (!res.ok) throw new Error("아이디 또는 비밀번호가 맞지 않아요. 다시 확인해 주세요.")
}

export async function signup(username: string, password: string) {
  const res = await fetch("/signup", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  })
  if (!res.ok) throw new Error(await errorText(res, "가입하지 못했어요. 입력값을 확인해 주세요."))
}
