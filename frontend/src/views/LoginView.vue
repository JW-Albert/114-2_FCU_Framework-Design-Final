<template>
  <div class="login-page">
    <div class="login-card">
      <h1>公司車輛管理系統</h1>
      <h2>登入</h2>

      <form @submit.prevent="handleLogin">
        <div class="field">
          <label>電子郵件</label>
          <input v-model="form.email" type="email" placeholder="your@email.com" required />
        </div>
        <div class="field">
          <label>密碼</label>
          <input v-model="form.password" type="password" placeholder="••••••••" required />
        </div>
        <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
        <button type="submit" :disabled="loading">
          {{ loading ? '登入中…' : '登入' }}
        </button>
      </form>

      <div class="system-status" :class="statusClass">
        <span class="status-dot"></span>
        <span>系統狀態：{{ statusText }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { authApi } from '../api/auth'
import { systemApi } from '../api/system'

const router = useRouter()
const auth = useAuthStore()

const form = ref({ email: '', password: '' })
const errorMsg = ref('')
const loading = ref(false)

// 系統健康狀態（Actuator）
const systemUp = ref<boolean | null>(null)
const statusText = computed(() =>
  systemUp.value === null ? '檢查中…' : systemUp.value ? '正常運作' : '無法連線'
)
const statusClass = computed(() =>
  systemUp.value === null ? 'checking' : systemUp.value ? 'up' : 'down'
)

onMounted(async () => {
  try {
    systemUp.value = (await systemApi.health()) === 'UP'
  } catch {
    systemUp.value = false
  }
})

async function handleLogin() {
  errorMsg.value = ''
  loading.value = true
  try {
    const { data } = await authApi.login(form.value.email, form.value.password)
    auth.setToken(data.token, data.roles, data.email, data.name, data.department)
    if (auth.isAdmin) {
      router.push('/admin/dashboard')
    } else if (auth.isManager) {
      router.push('/admin/review')
    } else {
      router.push('/employee/borrow')
    }
  } catch (e: any) {
    errorMsg.value = e.response?.data?.error ?? '登入失敗，請確認帳號密碼'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f4f8;
}

.login-card {
  background: white;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 380px;
}

h1 {
  font-size: 1.1rem;
  color: #64748b;
  margin-bottom: 0.25rem;
  text-align: center;
}

h2 {
  font-size: 1.6rem;
  color: #1e293b;
  margin-bottom: 1.75rem;
  text-align: center;
}

.field {
  margin-bottom: 1.1rem;
}

label {
  display: block;
  font-size: 0.85rem;
  font-weight: 600;
  color: #475569;
  margin-bottom: 0.35rem;
}

input {
  width: 100%;
  padding: 0.65rem 0.85rem;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.95rem;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

input:focus {
  outline: none;
  border-color: #3b82f6;
}

button {
  width: 100%;
  padding: 0.75rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  margin-top: 0.5rem;
  transition: background 0.2s;
}

button:hover:not(:disabled) {
  background: #2563eb;
}

button:disabled {
  background: #93c5fd;
  cursor: not-allowed;
}

.error {
  color: #ef4444;
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
}

.system-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  margin-top: 1.5rem;
  font-size: 0.78rem;
  color: #94a3b8;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #cbd5e1;
}

.system-status.up .status-dot {
  background: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18);
}

.system-status.down .status-dot {
  background: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.18);
}

.system-status.down {
  color: #ef4444;
}
</style>
