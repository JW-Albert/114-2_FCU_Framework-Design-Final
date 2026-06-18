import client from './client'

export interface Violation {
  id: string
  userId: string
  vehicleId: string
  borrowingId: string
  type: string
  description: string
  createdAt: string
}

export const violationsApi = {
  listAll: () => client.get<Violation[]>('/violations').then(r => r.data),
  listByUser: (userId: string) =>
    client.get<Violation[]>(`/violations/user/${userId}`).then(r => r.data),
}
