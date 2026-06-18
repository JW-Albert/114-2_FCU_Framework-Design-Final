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

/** 可選的違規類型（手動登錄用）。 */
export const VIOLATION_TYPES = [
  { value: 'OVERDUE', label: '超時還車' },
  { value: 'DAMAGE', label: '車輛損壞' },
  { value: 'ILLEGAL_PARKING', label: '違規停車' },
  { value: 'TRAFFIC_VIOLATION', label: '交通違規' },
  { value: 'OTHER', label: '其他' },
] as const

/** 取得違規類型的顯示名稱，未知類型回傳原值。 */
export function violationTypeLabel(type: string): string {
  return VIOLATION_TYPES.find(t => t.value === type)?.label ?? type
}

export const violationsApi = {
  listAll: () => client.get<Violation[]>('/violations').then(r => r.data),
  listByUser: (userId: string) =>
    client.get<Violation[]>(`/violations/user/${userId}`).then(r => r.data),
  create: (borrowingId: string, type: string, description: string) =>
    client.post<Violation>('/violations', { borrowingId, type, description }).then(r => r.data),
}
