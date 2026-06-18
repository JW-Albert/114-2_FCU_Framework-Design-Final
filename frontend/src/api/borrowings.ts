import client from './client'

export interface BorrowingRequest {
  id: string
  userId: string
  vehicleId: string
  periodStart: string
  periodEnd: string
  purpose: string
  state: string
  reviewNote: string | null
  createdAt: string
  updatedAt: string
  startMileage?: number
  endMileage?: number
}

export interface ConflictCheckResult {
  hasConflict: boolean
  conflicts: BorrowingRequest[]
}

export const borrowingsApi = {
  submit: (vehicleId: string, periodStart: string, periodEnd: string, purpose: string) =>
    client.post<BorrowingRequest>('/borrowings', { vehicleId, periodStart, periodEnd, purpose }),

  listMine: () => client.get<BorrowingRequest[]>('/borrowings/my'),

  listAll: () => client.get<BorrowingRequest[]>('/borrowings'),

  listPending: () => client.get<BorrowingRequest[]>('/borrowings/pending'),

  approve: (id: string, note?: string) =>
    client.post<BorrowingRequest>(`/borrowings/${id}/approve`, { note }),

  reject: (id: string, note?: string) =>
    client.post<BorrowingRequest>(`/borrowings/${id}/reject`, { note }),

  startUse: (id: string) =>
    client.post<BorrowingRequest>(`/borrowings/${id}/start`),

  complete: (id: string, endMileage: number) =>
    client.post<BorrowingRequest>(`/borrowings/${id}/complete`, { endMileage }),

  checkConflict: (vehicleId: string, start: string, end: string) =>
    client.get<ConflictCheckResult>('/borrowings/conflict-check', {
      params: { vehicleId, start, end }
    }).then(r => r.data),
}
