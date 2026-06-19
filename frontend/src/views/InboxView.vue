<template>
  <div class="page">
    <div class="header">
      <h2>收件匣<span v-if="unreadCount > 0" class="unread-pill">{{ unreadCount }} 則未讀</span></h2>
      <button class="btn secondary" :disabled="unreadCount === 0" @click="markAll">全部標為已讀</button>
    </div>

    <section class="card">
      <div v-if="loading" class="empty">載入中…</div>
      <div v-else-if="items.length === 0" class="empty">目前沒有訊息</div>
      <ul v-else class="inbox-list">
        <li
          v-for="n in items"
          :key="n.id"
          class="inbox-item"
          :class="{ unread: !n.read }"
          @click="open(n)"
        >
          <span class="dot" :class="{ on: !n.read }"></span>
          <span class="icon">{{ iconFor(n.type) }}</span>
          <div class="body">
            <div class="title-row">
              <span class="title">{{ n.title }}</span>
              <span class="time">{{ fmt(n.createdAt) }}</span>
            </div>
            <div class="content">{{ n.content }}</div>
          </div>
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { notificationsApi, type Notification } from '../api/notifications'
import { useNotificationStore } from '../stores/notifications'

const items = ref<Notification[]>([])
const loading = ref(false)
const notifStore = useNotificationStore()

const unreadCount = computed(() => items.value.filter(n => !n.read).length)

onMounted(load)

async function load() {
  loading.value = true
  try {
    items.value = await notificationsApi.list()
  } catch {
    items.value = []
  } finally {
    loading.value = false
  }
  await notifStore.refresh()
}

async function open(n: Notification) {
  if (n.read) return
  try {
    await notificationsApi.markRead(n.id)
    n.read = true
    await notifStore.refresh()
  } catch {
    // 忽略單筆標記失敗
  }
}

async function markAll() {
  try {
    await notificationsApi.markAllRead()
    items.value.forEach(n => (n.read = true))
    await notifStore.refresh()
  } catch {
    // 忽略
  }
}

function iconFor(type: string): string {
  const map: Record<string, string> = {
    BORROWING_SUBMITTED: '📥',
    BORROWING_APPROVED: '✅',
    BORROWING_REJECTED: '❌',
    BORROWING_STARTED: '🚗',
    BORROWING_COMPLETED: '🏁',
  }
  return map[type] ?? '🔔'
}

function fmt(iso: string) {
  if (!iso) return '-'
  return new Date(iso).toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}
</script>

<style scoped>
.page { max-width: 820px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; }
h2 { font-size: 1.5rem; color: #1e293b; display: flex; align-items: center; gap: 0.6rem; }
.unread-pill { font-size: 0.75rem; font-weight: 700; color: #b91c1c; background: #fee2e2; padding: 0.2rem 0.6rem; border-radius: 999px; }
.card { background: white; border-radius: 10px; box-shadow: 0 1px 6px rgba(0,0,0,.06); overflow: hidden; }
.empty { color: #94a3b8; font-size: 0.9rem; padding: 2rem; text-align: center; }
.inbox-list { list-style: none; }
.inbox-item { display: flex; align-items: flex-start; gap: 0.75rem; padding: 0.9rem 1.1rem; border-bottom: 1px solid #f1f5f9; cursor: pointer; transition: background .12s; }
.inbox-item:last-child { border-bottom: none; }
.inbox-item:hover { background: #f8fafc; }
.inbox-item.unread { background: #eff6ff; }
.inbox-item.unread:hover { background: #dbeafe; }
.dot { width: 9px; height: 9px; border-radius: 50%; margin-top: 0.45rem; flex-shrink: 0; background: transparent; }
.dot.on { background: #3b82f6; }
.icon { font-size: 1.15rem; line-height: 1.5; }
.body { flex: 1; min-width: 0; }
.title-row { display: flex; justify-content: space-between; align-items: baseline; gap: 0.75rem; }
.title { font-weight: 600; color: #1e293b; font-size: 0.95rem; }
.inbox-item.unread .title { font-weight: 700; }
.time { font-size: 0.75rem; color: #94a3b8; white-space: nowrap; }
.content { font-size: 0.85rem; color: #64748b; margin-top: 0.2rem; word-break: break-word; }
.btn { padding: 0.5rem 1rem; border-radius: 7px; font-size: 0.9rem; font-weight: 600; border: none; cursor: pointer; transition: all .15s; }
.btn.secondary { background: #f1f5f9; color: #475569; }
.btn.secondary:hover:not(:disabled) { background: #e2e8f0; }
.btn.secondary:disabled { opacity: .5; cursor: not-allowed; }
</style>
