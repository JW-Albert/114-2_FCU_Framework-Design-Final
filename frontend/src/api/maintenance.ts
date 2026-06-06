import client from './client'

export interface MaintenanceRecord {
  id: string
  vehicleId: string
  date: string
  items: string[]
  cost: number
  nextDueDate: string | null
  nextDueKm: number | null
  createdAt: string
}

export const maintenanceApi = {
  getByVehicle: (vehicleId: string) =>
    client.get<MaintenanceRecord[]>(`/maintenance/vehicle/${vehicleId}`),

  add: (record: {
    vehicleId: string
    date: string
    items: string[]
    cost: number
    nextDueDate?: string
    nextDueKm?: number
  }) => client.post<MaintenanceRecord>('/maintenance', record),

  delete: (id: string) => client.delete(`/maintenance/${id}`),
}
