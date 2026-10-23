import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { toast } from 'sonner'
import { useAuthStore } from './auth/authStore'
import type { AuthResponse } from './auth/types'

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (error && typeof error === 'object' && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    if (response?.data?.message) return response.data.message
  }
  return error instanceof Error && error.message ? error.message : fallback
}

const apiClient = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
})

let refreshPromise: Promise<string | null> | null = null

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().accessToken
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined
    const isRefreshRequest = originalRequest?.url?.endsWith('/auth/refresh')
    const isAuthRequest = originalRequest?.url?.includes('/auth/login') || originalRequest?.url?.includes('/auth/register')

    if (error.response?.status !== 401 || !originalRequest || originalRequest._retry || isRefreshRequest || isAuthRequest) {
      return Promise.reject(error)
    }

    originalRequest._retry = true
    refreshPromise ??= apiClient.post<AuthResponse>('/auth/refresh')
      .then(({ data }) => {
        useAuthStore.getState().setSession(data)
        return data.accessToken
      })
      .catch(() => {
        useAuthStore.getState().clearSession()
        toast.error('Tu sesión terminó. Inicia sesión nuevamente.')
        return null
      })
      .finally(() => { refreshPromise = null })

    const token = await refreshPromise
    if (!token) return Promise.reject(error)
    originalRequest.headers.Authorization = `Bearer ${token}`
    return apiClient(originalRequest)
  },
)

export { apiClient }
