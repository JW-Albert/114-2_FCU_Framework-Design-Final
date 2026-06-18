import client from './client'

export interface UserRecord {
  id: string
  name: string
  email: string
  roles: string[]
  createdAt: string
  department?: string
}

export const usersApi = {
  listAll: () => client.get<UserRecord[]>('/users'),

  getOne: (id: string) => client.get<UserRecord>(`/users/${id}`),

  create: (name: string, email: string, password: string, role: string, department?: string) =>
    client.post<UserRecord>('/users', { name, email, password, role, department }),

  update: (id: string, name: string, email: string, department?: string) =>
    client.put<UserRecord>(`/users/${id}`, { name, email, department }),

  changeRole: (id: string, role: string) =>
    client.patch<UserRecord>(`/users/${id}/role`, { role }),

  delete: (id: string) => client.delete(`/users/${id}`),
}
