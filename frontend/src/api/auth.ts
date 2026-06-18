import client from './client'

export interface AuthResponse {
  token: string
  email: string
  name: string
  roles: string[]
  department?: string
}

export const authApi = {
  login: (email: string, password: string) =>
    client.post<AuthResponse>('/auth/login', { email, password }),

  register: (name: string, email: string, password: string, role: string) =>
    client.post<AuthResponse>('/auth/register', { name, email, password, role }),
}
