import Cookies from "js-cookie"
import { AuthResponse, LoginRequest } from "@/types"
import { api } from "./api"

export const authService = {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>("/auth/login", credentials)

    if (response.token) {
      Cookies.set("jwt_token", response.token, { expires: 7 })
      Cookies.set("username", response.username)
    }

    return response
  },

  logout() {
    Cookies.remove("jwt_token")
    Cookies.remove("username")
    if (typeof window !== "undefined") {
      window.location.href = "/login"
    }
  },

  getToken(): string | undefined {
    return Cookies.get("jwt_token")
  },

  getUsername(): string | undefined {
    return Cookies.get("username")
  },

  isAuthenticated(): boolean {
    return !!this.getToken()
  },

  hasRole(role: string): boolean {
    // You can decode JWT to check roles or store roles in cookies
    return true // Simplified for now
  },
}
