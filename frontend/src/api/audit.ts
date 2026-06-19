import client from './client'

export interface AuditLog {
  id: string
  action: string
  detail: string
  targetId: string | null
  createdAt: string
}

export const auditApi = {
  list: () => client.get<AuditLog[]>('/audit-logs').then(r => r.data),
}
