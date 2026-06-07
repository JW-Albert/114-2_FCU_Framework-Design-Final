import client from './client'

export interface Vehicle {
  id: string
  plate: string
  model: string
  year: number
  status: string
  createdAt: string
}

export const vehiclesApi = {
  listAll: () => client.get<Vehicle[]>('/vehicles'),

  findAvailable: (start: string, end: string) =>
    client.get<Vehicle[]>('/vehicles/available', { params: { start, end } }),

  getOne: (id: string) => client.get<Vehicle>(`/vehicles/${id}`),

  create: (plate: string, model: string, year: number) =>
    client.post<Vehicle>('/vehicles', { plate, model, year }),

  update: (id: string, plate: string, model: string, year: number) =>
    client.put<Vehicle>(`/vehicles/${id}`, { plate, model, year }),

  delete: (id: string) => client.delete(`/vehicles/${id}`),
}
