import { create } from 'zustand'
import { apiClient } from '../apiClient'
import type { AuthResponse, AuthUser } from './types'
import { userFromResponse } from './types'

type AuthState = {
  isAuthenticated: boolean
  user: AuthUser | null
  accessToken: string | null
  isHydrating: boolean
  setSession: (response: AuthResponse) => void
  clearSession: () => void
  logout: () => Promise<void>
  hydrate: () => Promise<void>
}

export const useAuthStore = create<AuthState>((set) => ({
  isAuthenticated: false,
  user: null,
  accessToken: null,
  isHydrating: true,

  setSession: (response) => set({
    isAuthenticated: true,
    user: userFromResponse(response),
    accessToken: response.accessToken,
    isHydrating: false,
  }),

  clearSession: () => set({
    isAuthenticated: false,
    user: null,
    accessToken: null,
    isHydrating: false,
  }),

  logout: async () => {
    try {
      await apiClient.post('/auth/logout')
    } finally {
      set({ isAuthenticated: false, user: null, accessToken: null, isHydrating: false })
    }
  },

  hydrate: async () => {
    try {
      const { data } = await apiClient.post<AuthResponse>('/auth/refresh')
      set({
        isAuthenticated: true,
        user: userFromResponse(data),
        accessToken: data.accessToken,
        isHydrating: false,
      })
    } catch {
      set({ isAuthenticated: false, user: null, accessToken: null, isHydrating: false })
    }
  },
}))
