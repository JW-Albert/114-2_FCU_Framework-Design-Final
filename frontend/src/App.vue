<template>
  <div id="app">
    <nav v-if="auth.token" class="navbar">
      <span class="brand">🚗 車輛管理系統</span>
      <div class="nav-links">
        <template v-if="auth.isAdmin">
          <router-link to="/admin/dashboard">統計總覽</router-link>
          <router-link to="/admin/review">審核申請</router-link>
          <router-link to="/admin/vehicles">車輛管理</router-link>
          <router-link to="/admin/maintenance">保養管理</router-link>
          <router-link to="/admin/users">使用者管理</router-link>
          <router-link to="/admin/violations">違規記錄</router-link>
          <router-link to="/admin/calendar">借用日曆</router-link>
        </template>
        <template v-else-if="auth.isManager">
          <router-link to="/employee/borrow">借車申請</router-link>
          <router-link to="/admin/review">借車申請審核</router-link>
          <router-link to="/admin/violations">違規記錄</router-link>
          <router-link to="/admin/calendar">借用日曆</router-link>
        </template>
        <template v-else>
          <router-link to="/employee/borrow">借車申請</router-link>
        </template>
      </div>
      <div class="nav-right">
        <router-link to="/inbox" class="inbox-link" :class="{ 'has-unread': notif.unreadCount > 0 }" title="收件匣">
          <span class="inbox-icon">📥</span>
          <span v-if="notif.unreadCount > 0" class="inbox-badge">{{ notif.unreadCount > 99 ? '99+' : notif.unreadCount }}</span>
        </router-link>
        <span class="user-name">{{ auth.userName }}</span>
        <span class="role-badge" :class="auth.isAdmin ? 'admin' : auth.isManager ? 'manager' : 'employee'">
          {{ auth.isAdmin ? '管理員' : auth.isManager ? '主管' : '員工' }}
        </span>
        <button class="logout-btn" @click="handleLogout">登出</button>
      </div>
    </nav>

    <main :class="{ 'with-nav': auth.token }">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { useNotificationStore } from './stores/notifications'

const auth = useAuthStore()
const notif = useNotificationStore()
const router = useRouter()

let timer: number | undefined

function startPolling() {
  notif.refresh()
  stopPolling()
  timer = window.setInterval(() => notif.refresh(), 30000)
}

function stopPolling() {
  if (timer) {
    clearInterval(timer)
    timer = undefined
  }
}

onMounted(() => {
  if (auth.token) startPolling()
})

onUnmounted(stopPolling)

// 登入後開始輪詢未讀數，登出時停止並歸零（狀態燈熄滅）
watch(() => auth.token, (token) => {
  if (token) {
    startPolling()
  } else {
    stopPolling()
    notif.reset()
  }
})

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: #1e293b;
  background: #f8fafc;
}

#app {
  min-height: 100vh;
}

.navbar {
  display: flex;
  align-items: center;
  padding: 0 1.5rem;
  height: 56px;
  background: #1e293b;
  color: white;
  position: sticky;
  top: 0;
  z-index: 100;
  gap: 1.5rem;
}

.brand {
  font-weight: 700;
  font-size: 1.05rem;
  white-space: nowrap;
}

.nav-links {
  display: flex;
  gap: 0.25rem;
  flex: 1;
}

.nav-links a {
  color: #94a3b8;
  text-decoration: none;
  padding: 0.4rem 0.85rem;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: all 0.15s;
}

.nav-links a:hover,
.nav-links a.router-link-active {
  color: white;
  background: #334155;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-left: auto;
}

.inbox-link {
  position: relative;
  display: inline-flex;
  align-items: center;
  text-decoration: none;
  padding: 0.3rem 0.4rem;
  border-radius: 6px;
  transition: background 0.15s;
}

.inbox-link:hover,
.inbox-link.router-link-active {
  background: #334155;
}

.inbox-icon {
  font-size: 1.1rem;
  line-height: 1;
  filter: grayscale(0.4);
}

.inbox-link.has-unread .inbox-icon {
  filter: none;
}

.inbox-badge {
  position: absolute;
  top: -2px;
  right: -3px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: #ef4444;
  color: white;
  font-size: 0.65rem;
  font-weight: 700;
  line-height: 16px;
  text-align: center;
  border-radius: 999px;
  box-shadow: 0 0 0 2px #1e293b;
}

.user-name {
  font-size: 0.9rem;
  color: #e2e8f0;
}

.role-badge {
  font-size: 0.75rem;
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  font-weight: 600;
}

.role-badge.admin {
  background: #3b82f6;
  color: white;
}

.role-badge.manager {
  background: #8b5cf6;
  color: white;
}

.role-badge.employee {
  background: #10b981;
  color: white;
}

.logout-btn {
  background: transparent;
  border: 1px solid #475569;
  color: #94a3b8;
  padding: 0.3rem 0.85rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.15s;
}

.logout-btn:hover {
  border-color: #ef4444;
  color: #ef4444;
}

main.with-nav {
  padding: 1.5rem;
}
</style>
