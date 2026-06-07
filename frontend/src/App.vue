<template>
  <div id="app">
    <nav v-if="auth.token" class="navbar">
      <span class="brand">🚗 車輛管理系統</span>
      <div class="nav-links">
        <template v-if="auth.isAdmin">
          <router-link to="/admin/review">審核申請</router-link>
          <router-link to="/admin/vehicles">車輛管理</router-link>
          <router-link to="/admin/maintenance">保養管理</router-link>
          <router-link to="/admin/users">使用者管理</router-link>
        </template>
        <template v-else>
          <router-link to="/employee/borrow">借車申請</router-link>
        </template>
      </div>
      <div class="nav-right">
        <span class="user-name">{{ auth.userName }}</span>
        <span class="role-badge" :class="auth.isAdmin ? 'admin' : 'employee'">
          {{ auth.isAdmin ? '管理員' : '員工' }}
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
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const router = useRouter()

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
