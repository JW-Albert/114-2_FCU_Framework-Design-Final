<template>
  <div class="page">
    <h2>使用者帳號管理</h2>

    <!-- 新增帳號表單 -->
    <section class="card">
      <h3>{{ editId ? '編輯帳號' : '新增帳號' }}</h3>
      <div class="form-row">
        <div class="field">
          <label>姓名</label>
          <input v-model="form.name" placeholder="王小明" />
        </div>
        <div class="field">
          <label>Email</label>
          <input v-model="form.email" type="email" placeholder="user@company.com" />
        </div>
        <div v-if="!editId" class="field">
          <label>密碼</label>
          <input v-model="form.password" type="password" placeholder="至少 6 位" />
        </div>
        <div class="field">
          <label>身分</label>
          <select v-model="form.role">
            <option value="EMPLOYEE">員工</option>
            <option value="MANAGER">部門主管</option>
            <option value="ADMIN">管理員</option>
          </select>
        </div>
        <div class="field">
          <label>部門</label>
          <input v-model="form.department" placeholder="例：資訊部" />
        </div>
        <div class="form-actions">
          <button class="btn primary" @click="saveUser" :disabled="saving">
            {{ saving ? '儲存中…' : editId ? '更新' : '新增' }}
          </button>
          <button v-if="editId" class="btn secondary" @click="cancelEdit">取消</button>
        </div>
      </div>
      <p v-if="formError" class="error">{{ formError }}</p>
    </section>

    <!-- 使用者列表 -->
    <section class="card">
      <div class="section-header">
        <h3>帳號清單</h3>
        <button class="btn secondary" @click="loadUsers">重新整理</button>
      </div>

      <div v-if="loading" class="empty">載入中…</div>
      <div v-else-if="users.length === 0" class="empty">尚無帳號資料</div>
      <table v-else class="table">
        <thead>
          <tr>
            <th>姓名</th>
            <th>Email</th>
            <th>身分</th>
            <th>部門</th>
            <th>建立時間</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td><strong>{{ u.name }}</strong></td>
            <td>{{ u.email }}</td>
            <td>
              <span v-for="r in u.roles" :key="r" :class="['badge', roleClass(r)]">
                {{ roleLabel(r) }}
              </span>
            </td>
            <td>{{ u.department ?? '—' }}</td>
            <td>{{ fmt(u.createdAt) }}</td>
            <td>
              <div class="action-btns">
                <button class="btn-icon edit" @click="startEdit(u)" title="編輯">✏️</button>
                <button class="btn-icon del" @click="deleteUser(u.id)" title="刪除">🗑️</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { usersApi, type UserRecord } from '../api/users'

const users = ref<UserRecord[]>([])
const loading = ref(false)
const saving = ref(false)
const formError = ref('')
const editId = ref<string | null>(null)
const form = ref({ name: '', email: '', password: '', role: 'EMPLOYEE', department: '' })

onMounted(loadUsers)

async function loadUsers() {
  loading.value = true
  try {
    const { data } = await usersApi.listAll()
    users.value = data
  } catch {
    users.value = []
  } finally {
    loading.value = false
  }
}

async function saveUser() {
  const { name, email, password, role, department } = form.value
  if (!name.trim() || !email.trim()) {
    formError.value = '請填寫姓名與 Email'
    return
  }
  if (!editId.value && !password.trim()) {
    formError.value = '請填寫密碼'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    if (editId.value) {
      await usersApi.update(editId.value, name, email, department || undefined)
      const currentUser = users.value.find(u => u.id === editId.value)
      if (currentUser && currentUser.roles[0] !== role) {
        await usersApi.changeRole(editId.value, role)
      }
    } else {
      await usersApi.create(name, email, password, role, department || undefined)
    }
    cancelEdit()
    await loadUsers()
  } catch (e: any) {
    formError.value = e.response?.data?.error ?? '儲存失敗'
  } finally {
    saving.value = false
  }
}

async function deleteUser(id: string) {
  if (!confirm('確定刪除此帳號？此操作無法復原。')) return
  try {
    await usersApi.delete(id)
    await loadUsers()
  } catch (e: any) {
    alert(e.response?.data?.error ?? '刪除失敗')
  }
}

function startEdit(u: UserRecord) {
  editId.value = u.id
  form.value = { name: u.name, email: u.email, password: '', role: u.roles[0] ?? 'EMPLOYEE', department: u.department ?? '' }
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function cancelEdit() {
  editId.value = null
  form.value = { name: '', email: '', password: '', role: 'EMPLOYEE', department: '' }
  formError.value = ''
}

function fmt(iso: string) {
  return new Date(iso).toLocaleDateString('zh-TW')
}

function roleLabel(r: string) {
  if (r === 'ADMIN') return '管理員'
  if (r === 'MANAGER') return '部門主管'
  return '員工'
}

function roleClass(r: string) {
  if (r === 'ADMIN') return 'admin'
  if (r === 'MANAGER') return 'manager'
  return 'employee'
}
</script>

<style scoped>
.page { max-width: 960px; margin: 0 auto; }
h2 { font-size: 1.5rem; margin-bottom: 1.25rem; color: #1e293b; }
.card { background: white; border-radius: 10px; padding: 1.5rem; box-shadow: 0 1px 6px rgba(0,0,0,.06); margin-bottom: 1.25rem; }
h3 { font-size: 1.05rem; margin-bottom: 1rem; color: #334155; }
.form-row { display: flex; gap: 1rem; align-items: flex-end; flex-wrap: wrap; }
.field { display: flex; flex-direction: column; gap: 0.3rem; }
label { font-size: 0.8rem; font-weight: 600; color: #64748b; }
input, select { padding: 0.55rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 7px; font-size: 0.9rem; background: white; }
input:focus, select:focus { outline: none; border-color: #3b82f6; }
.form-actions { display: flex; gap: 0.5rem; align-items: flex-end; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
.table th { background: #f8fafc; padding: 0.6rem 0.75rem; text-align: left; font-size: 0.8rem; color: #64748b; border-bottom: 1px solid #e2e8f0; }
.table td { padding: 0.65rem 0.75rem; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
.badge { padding: 0.2rem 0.6rem; border-radius: 999px; font-size: 0.75rem; font-weight: 600; margin-right: 0.25rem; }
.badge.admin { background: #ede9fe; color: #6d28d9; }
.badge.manager { background: #fef3c7; color: #92400e; }
.badge.employee { background: #dbeafe; color: #1d4ed8; }
.action-btns { display: flex; gap: 0.4rem; }
.btn-icon { background: none; border: none; cursor: pointer; font-size: 1rem; padding: 0.2rem 0.4rem; border-radius: 4px; transition: background .15s; }
.btn-icon.edit:hover { background: #eff6ff; }
.btn-icon.del:hover { background: #fee2e2; }
.btn { padding: 0.55rem 1.1rem; border-radius: 7px; font-size: 0.9rem; font-weight: 600; border: none; cursor: pointer; transition: all .15s; }
.btn.primary { background: #3b82f6; color: white; }
.btn.primary:hover:not(:disabled) { background: #2563eb; }
.btn.primary:disabled { background: #93c5fd; cursor: not-allowed; }
.btn.secondary { background: #f1f5f9; color: #475569; }
.btn.secondary:hover { background: #e2e8f0; }
.empty { color: #94a3b8; font-size: 0.9rem; padding: 1rem 0; }
.error { color: #ef4444; font-size: 0.85rem; margin-top: 0.5rem; }
</style>
