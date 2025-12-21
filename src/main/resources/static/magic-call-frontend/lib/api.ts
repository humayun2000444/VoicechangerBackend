import axios, { AxiosInstance, AxiosRequestConfig } from "axios"
import Cookies from "js-cookie"

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api"

class ApiClient {
  private client: AxiosInstance

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        "Content-Type": "application/json",
      },
    })

    // Request interceptor to add auth token
    this.client.interceptors.request.use(
      (config) => {
        const token = Cookies.get("jwt_token")
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // Response interceptor to handle 401 errors
    this.client.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          Cookies.remove("jwt_token")
          Cookies.remove("username")
          if (typeof window !== "undefined") {
            window.location.href = "/login"
          }
        }
        return Promise.reject(error)
      }
    )
  }

  // Helper to unwrap API response
  private unwrapResponse<T>(response: any): T {
    // If response has a 'data' field, extract it (wrapped response)
    if (response && typeof response === 'object' && 'data' in response) {
      return response.data as T
    }
    // Otherwise return as-is (direct response)
    return response as T
  }

  async get<T>(url: string, config?: AxiosRequestConfig) {
    const response = await this.client.get(url, config)
    return this.unwrapResponse<T>(response.data)
  }

  async post<T>(url: string, data?: any, config?: AxiosRequestConfig) {
    const response = await this.client.post(url, data, config)
    return this.unwrapResponse<T>(response.data)
  }

  async put<T>(url: string, data?: any, config?: AxiosRequestConfig) {
    const response = await this.client.put(url, data, config)
    return this.unwrapResponse<T>(response.data)
  }

  async delete<T>(url: string, config?: AxiosRequestConfig) {
    const response = await this.client.delete(url, config)
    return this.unwrapResponse<T>(response.data)
  }

  async patch<T>(url: string, data?: any, config?: AxiosRequestConfig) {
    const response = await this.client.patch(url, data, config)
    return this.unwrapResponse<T>(response.data)
  }
}

export const api = new ApiClient()
