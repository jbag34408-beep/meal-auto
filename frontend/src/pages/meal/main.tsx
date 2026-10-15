import { StrictMode } from "react"
import { createRoot } from "react-dom/client"
import "@/index.css"
import MealPage from "./MealPage"

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <MealPage />
  </StrictMode>
)
