import { defineStore } from 'pinia'
import { ref } from 'vue'
import { notificationsApi } from '../api/notifications'

/**
 * 站內通知未讀數狀態（導覽列狀態燈用）。
 *
 * App.vue 載入後定期 refresh，InboxView 標記已讀後亦呼叫 refresh 同步狀態燈。
 */
export const useNotificationStore = defineStore('notifications', () => {
  const unreadCount = ref(0)

  async function refresh() {
    try {
      unreadCount.value = await notificationsApi.unreadCount()
    } catch {
      unreadCount.value = 0
    }
  }

  function reset() {
    unreadCount.value = 0
  }

  return { unreadCount, refresh, reset }
})
