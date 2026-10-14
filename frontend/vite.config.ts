import path from "node:path"
import { defineConfig } from "vite"
import react from "@vitejs/plugin-react"
import tailwindcss from "@tailwindcss/vite"

// Spring Boot 서버 (./gradlew bootRun)
const API = "http://localhost:8080"

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: { "@": path.resolve(import.meta.dirname, "src") },
  },
  build: {
    // 빌드 결과를 Spring Boot의 static 폴더로 → 같은 주소에서 화면과 API를 함께 서비스
    outDir: path.resolve(import.meta.dirname, "../src/main/resources/static"),
    emptyOutDir: false, // 아직 React로 옮기지 않은 index.html, landing.html은 남겨 둠
    assetsDir: "assets",
    rollupOptions: {
      // React로 옮긴 화면을 하나씩 여기에 추가
      input: {
        login: path.resolve(import.meta.dirname, "login.html"),
      },
    },
  },
  server: {
    port: 5173,
    // 개발 중(npm run dev): 화면은 Vite가, API·아직 안 옮긴 화면은 Spring Boot가 응답
    proxy: {
      "^/login$": API,
      "/signup": API,
      "/logout": API,
      "/me": API,
      "/meals": API,
      "/foods": API,
      "/summary": API,
      "/recommendation": API,
      "/index.html": API,
      "/landing.html": API,
    },
  },
})
