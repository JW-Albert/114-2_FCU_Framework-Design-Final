<template>
  <div class="page">
    <h2>車輛管理</h2>

    <!-- 新增/編輯表單 -->
    <section class="card">
      <h3>{{ editId ? '編輯車輛' : '新增車輛' }}</h3>
      <div class="form-row">
        <div class="field">
          <label>車牌號碼</label>
          <input v-model="form.plate" placeholder="ABC-1234" />
        </div>
        <div class="field">
          <label>車款</label>
          <input v-model="form.model" placeholder="Toyota Camry" />
        </div>
        <div class="field">
          <label>年份</label>
          <input v-model.number="form.year" type="number" min="2000" max="2100" placeholder="2024" />
        </div>
        <div class="form-actions">
          <button class="btn primary" @click="saveVehicle" :disabled="saving">
            {{ saving ? '儲存中…' : editId ? '更新' : '新增' }}
          </button>
          <button v-if="editId" class="btn secondary" @click="cancelEdit">取消</button>
        </div>
      </div>
      <p v-if="formError" class="error">{{ formError }}</p>
    </section>

    <!-- 車輛列表 -->
    <section class="card">
      <div class="section-header">
        <h3>車輛清單</h3>
        <button class="btn secondary" @click="loadVehicles">重新整理</button>
      </div>

      <div v-if="vehicles.length === 0" class="empty">尚無車輛資料</div>
      <table v-else class="table">
        <thead>
          <tr>
            <th>車牌</th>
            <th>車款</th>
            <th>年份</th>
            <th>狀態</th>
            <th>目前里程</th>
            <th>建立時間</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="v in vehicles" :key="v.id">
            <td><strong>{{ v.plate }}</strong></td>
            <td>{{ v.model }}</td>
            <td>{{ v.year }}</td>
            <td><span :class="['badge', statusClass(v.status)]">{{ statusLabel(v.status) }}</span></td>
            <td>{{ v.currentMileage.toLocaleString() }} km</td>
            <td>{{ fmt(v.createdAt) }}</td>
            <td>
              <div class="action-btns">
                <button class="btn-icon edit" @click="startEdit(v)" title="編輯">✏️</button>
                <button class="btn-icon del" @click="deleteVehicle(v.id)" title="刪除">🗑️</button>
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
import { vehiclesApi, type Vehicle } from '../api/vehicles'

const vehicles = ref<Vehicle[]>([])
const saving = ref(false)
const formError = ref('')
const editId = ref<string | null>(null)
const form = ref({ plate: '', model: '', year: new Date().getFullYear() })

onMounted(loadVehicles)

async function loadVehicles() {
  try {
    const { data } = await vehiclesApi.listAll()
    vehicles.value = data
  } catch {}
}

async function saveVehicle() {
  const { plate, model, year } = form.value
  if (!plate.trim() || !model.trim() || !year) {
    formError.value = '請填寫所有欄位'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    if (editId.value) {
      await vehiclesApi.update(editId.value, plate, model, year)
    } else {
      await vehiclesApi.create(plate, model, year)
    }
    cancelEdit()
    await loadVehicles()
  } catch (e: any) {
    formError.value = e.response?.data?.error ?? '儲存失敗'
  } finally {
    saving.value = false
  }
}

async function deleteVehicle(id: string) {
  if (!confirm('確定刪除此車輛？')) return
  try {
    await vehiclesApi.delete(id)
    await loadVehicles()
  } catch (e: any) {
    alert(e.response?.data?.error ?? '刪除失敗')
  }
}

function startEdit(v: Vehicle) {
  editId.value = v.id
  form.value = { plate: v.plate, model: v.model, year: v.year }
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function cancelEdit() {
  editId.value = null
  form.value = { plate: '', model: '', year: new Date().getFullYear() }
  formError.value = ''
}

function fmt(iso: string) {
  return new Date(iso).toLocaleDateString('zh-TW')
}

function statusLabel(s: string) {
  const map: Record<string, string> = {
    AVAILABLE: '可用', IN_USE: '使用中', MAINTENANCE: '維修中',
  }
  return map[s] ?? s
}

function statusClass(s: string) {
  const map: Record<string, string> = {
    AVAILABLE: 'available', IN_USE: 'inuse', MAINTENANCE: 'maintenance',
  }
  return map[s] ?? ''
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
input { padding: 0.55rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 7px; font-size: 0.9rem; }
input:focus { outline: none; border-color: #3b82f6; }
.form-actions { display: flex; gap: 0.5rem; align-items: flex-end; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
.table th { background: #f8fafc; padding: 0.6rem 0.75rem; text-align: left; font-size: 0.8rem; color: #64748b; border-bottom: 1px solid #e2e8f0; }
.table td { padding: 0.65rem 0.75rem; border-bottom: 1px solid #f1f5f9; }
.badge { padding: 0.2rem 0.6rem; border-radius: 999px; font-size: 0.75rem; font-weight: 600; }
.badge.available { background: #dcfce7; color: #15803d; }
.badge.inuse { background: #dbeafe; color: #1d4ed8; }
.badge.maintenance { background: #fef9c3; color: #a16207; }
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
