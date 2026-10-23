export type AuthUser = {
  userId: string
  email: string
  name: string
  role: 'STUDENT' | 'ADMIN'
}

export type AuthResponse = {
  accessToken: string
  tokenType: 'Bearer'
  expiresIn: number
  userId: string
  email: string
  name: string
  role: AuthUser['role']
}

export function userFromResponse(response: AuthResponse): AuthUser {
  return {
    userId: response.userId,
    email: response.email,
    name: response.name,
    role: response.role,
  }
}
