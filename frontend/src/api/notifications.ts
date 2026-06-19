import client from './client'

export interface Notification {
  id: string
  type: string
  title: string
  content: string
  read: boolean
  relatedBorrowingId: string | null
  createdAt: string
}

export const notificationsApi = {
  list: () => client.get<Notification[]>('/notifications').then(r => r.data),
  unreadCount: () =>
    client.get<{ count: number }>('/notifications/unread-count').then(r => r.data.count),
  markRead: (id: string) => client.post(`/notifications/${id}/read`),
  markAllRead: () => client.post('/notifications/read-all'),
}
