// CSRF 토큰: 서버가 XSRF-TOKEN 쿠키로 내려 준 값을 POST·PUT·DELETE 요청의 X-XSRF-TOKEN 헤더로 돌려보냄
// (세션 쿠키 로그인이라 다른 사이트가 몰래 보낸 요청을 서버가 구분할 수 있게)

const COOKIE = "XSRF-TOKEN"
const HEADER = "X-XSRF-TOKEN"
const SAFE_METHODS = new Set(["GET", "HEAD", "OPTIONS"])

function readCookie() {
  const match = document.cookie.split("; ").find((c) => c.startsWith(`${COOKIE}=`))
  return match ? decodeURIComponent(match.slice(COOKIE.length + 1)) : ""
}

// 쿠키가 아직 없으면(첫 방문, 로그인·로그아웃 직후 토큰이 바뀜) 서버에 한 번 받아 옴
async function csrfToken() {
  let token = readCookie()
  if (!token) {
    await fetch("/csrf", { credentials: "same-origin" })
    token = readCookie()
  }
  return token
}

// fetch와 같지만, 데이터를 바꾸는 요청에는 CSRF 토큰 헤더를 붙임
export async function secureFetch(url: string, init: RequestInit = {}) {
  const method = (init.method ?? "GET").toUpperCase()
  if (SAFE_METHODS.has(method)) return fetch(url, init)

  const headers = new Headers(init.headers)
  headers.set(HEADER, await csrfToken())
  return fetch(url, { ...init, headers })
}
