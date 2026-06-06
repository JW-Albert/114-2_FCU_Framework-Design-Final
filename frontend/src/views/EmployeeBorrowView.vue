<template>
  <div class="page">
    <h2>借車申請</h2>

    <!-- 搜尋可用車輛 -->
    <section class="card">
      <h3>查詢可用車輛</h3>
      <div class="search-row">
        <div class="field">
          <label>開始時間</label>
          <input v-model="searchStart" type="datetime-local" />
        </div>
        <div class="field">
          <label>結束時間</label>
          <input v-model="searchEnd" type="datetime-local" />
        </div>
        <button class="btn primary" @click="searchVehicles" :disabled="searching">
          {{ searching ? '查詢中…' : '查詢' }}
        </button>
      </div>

      <div v-if="availableVehicles.length > 0" class="vehicle-list">
        <div
          v-for="v in availableVehicles"
          :key="v.id"
          class="vehicle-card"
          :class="{ selected: selectedVehicleId === v.id }"
          @click="selectedVehicleId = v.id"
        >
          <strong>{{ v.plate }}</strong>
          <span>{{ v.model }} ({{ v.year }})</span>
          <span class="status-available">可用</span>
        </div>
      </div>
      <p v-else-if="searched" class="empty">此時段無可用車輛</p>
    </section>

    <!-- 送出申請 -->
    <section class="card" v-if="selectedVehicleId">
      <h3>送出申請</h3>
      <div class="field">
        <label>用途說明</label>
        <textarea v-model="purpose" rows="3" placeholder="請填寫借車目的…"></textarea>
      </div>
      <p v-if="submitError" class="error">{{ submitError }}</p>
      <button class="btn primary" @click="submitRequest" :disabled="submitting">
        {{ submitting ? '送出中…' : '送出申請' }}
      </button>
    </section>

    <!-- 我的申請記錄 -->
    <section class="card">
      <div class="section-header">
        <h3>我的申請記錄</h3>
        <button class="btn secondary" @click="loadMyRequests">重新整理</button>
      </div>

      <div v-if="myRequests.length === 0" class="empty">尚無申請記錄</div>
      <table v-else class="table">
        <thead>
          <tr>
            <th>車牌</th>
            <th>開始時間</th>
            <th>結束時間</th>
            <th>目的</th>
            <th>狀態</th>
            <th>備注</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in myRequests" :key="r.id">
            <td>{{ vehicleMap[r.vehicleId] ?? r.vehicleId.slice(0, 8) }}</td>
            <td>{{ fmt(r.periodStart) }}</td>
            <td>{{ fmt(r.periodEnd) }}</td>
            <td>{{ r.purpose }}</td>
            <td><span :class="['badge', stateClass(r.state)]">{{ stateLabel(r.state) }}</span></td>
            <td>{{ r.reviewNote ?? '—' }}</td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { vehiclesApi, type Vehicle } from '../api/vehicles'
import { borrowingsApi, type BorrowingRequest } from '../api/borrowings'

const searchStart = ref('')
const searchEnd = ref('')
const availableVehicles = ref<Vehicle[]>([])
const searched = ref(false)
const searching = ref(false)
const selectedVehicleId = ref<string | null>(null)
const purpose = ref('')
const submitting = ref(false)
const submitError = ref('')
const myRequests = ref<BorrowingRequest[]>([])
const vehicleMap = ref<Record<string, string>>({})

onMounted(async () => {
  await loadMyRequests()
  await loadVehicleMap()
})

async function loadVehicleMap() {
  try {
    const { data } = await vehiclesApi.listAll()
    data.forEach(v => { vehicleMap.value[v.id] = v.plate })
  } catch {}
}

async function searchVehicles() {
  if (!searchStart.value || !searchEnd.value) return
  searching.value = true
  searched.value = false
  try {
    const start = new Date(searchStart.value).toISOString()
    const end = new Date(searchEnd.value).toISOString()
    const { data } = await vehiclesApi.findAvailable(start, end)
    availableVehicles.value = data
    searched.value = true
    selectedVehicleId.value = null
  } catch {
    availableVehicles.value = []
  } finally {
    searching.value = false
  }
}

async function submitRequest() {
  if (!selectedVehicleId.value || !purpose.value.trim()) {
    submitError.value = '請選擇車輛並填寫用途'
    return
  }
  submitting.value = true
  submitError.value = ''
  try {
    const start = new Date(searchStart.value).toISOString()
    const end = new Date(searchEnd.value).toISOString()
    await borrowingsApi.submit(selectedVehicleId.value, start, end, purpose.value)
    purpose.value = ''
    selectedVehicleId.value = null
    availableVehicles.value = []
    searched.value = false
    await loadMyRequests()
  } catch (e: any) {
    submitError.value = e.response?.data?.error ?? '送出失敗'
  } finally {
    submitting.value = false
  }
}

async function loadMyRequests() {
  try {
    const { data } = await borrowingsApi.listMine()
    myRequests.value = data.sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )
  } catch {}
}

function fmt(iso: string) {
  return new Date(iso).toLocaleString('zh-TW', { hour12: false })
}

function stateLabel(state: string) {
  const map: Record<string, string> = {
    PENDING: '待審核', APPROVED: '已核准', REJECTED: '已拒絕',
    IN_USE: '使用中', RETURNED: '已還車',
  }
  return map[state] ?? state
}

function stateClass(state: string) {
  const map: Record<string, string> = {
    PENDING: 'pending', APPROVED: 'approved', REJECTED: 'rejected',
    IN_USE: 'inuse', RETURNED: 'returned',
  }
  return map[state] ?? ''
}
</script>

<style scoped>
.page { max-width: 960px; margin: 0 auto; }
h2 { font-size: 1.5rem; margin-bottom: 1.25rem; color: #1e293b; }
.card { background: white; border-radius: 10px; padding: 1.5rem; box-shadow: 0 1px 6px rgba(0,0,0,.06); margin-bottom: 1.25rem; }
h3 { font-size: 1.05rem; margin-bottom: 1rem; color: #334155; }
.search-row { display: flex; gap: 1rem; align-items: flex-end; flex-wrap: wrap; }
.field { display: flex; flex-direction: column; gap: 0.3rem; }
label { font-size: 0.8rem; font-weight: 600; color: #64748b; }
input, textarea { padding: 0.55rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 7px; font-size: 0.9rem; }
input:focus, textarea:focus { outline: none; border-color: #3b82f6; }
.vehicle-list { display: flex; gap: 0.75rem; flex-wrap: wrap; margin-top: 1rem; }
.vehicle-card { border: 2px solid #e2e8f0; border-radius: 8px; padding: 0.75rem 1rem; cursor: pointer; display: flex; flex-direction: column; gap: 0.2rem; transition: all .15s; min-width: 160px; }
.vehicle-card:hover { border-color: #93c5fd; }
.vehicle-card.selected { border-color: #3b82f6; background: #eff6ff; }
.status-available { font-size: 0.75rem; color: #10b981; font-weight: 600; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
.table th { background: #f8fafc; padding: 0.6rem 0.75rem; text-align: left; font-size: 0.8rem; color: #64748b; border-bottom: 1px solid #e2e8f0; }
.table td { padding: 0.65rem 0.75rem; border-bottom: 1px solid #f1f5f9; }
.badge { padding: 0.2rem 0.6rem; border-radius: 999px; font-size: 0.75rem; font-weight: 600; }
.badge.pending { background: #fef9c3; color: #a16207; }
.badge.approved { background: #dcfce7; color: #15803d; }
.badge.rejected { background: #fee2e2; color: #b91c1c; }
.badge.inuse { background: #dbeafe; color: #1d4ed8; }
.badge.returned { background: #f1f5f9; color: #475569; }
.btn { padding: 0.55rem 1.1rem; border-radius: 7px; font-size: 0.9rem; font-weight: 600; border: none; cursor: pointer; transition: all .15s; }
.btn.primary { background: #3b82f6; color: white; }
.btn.primary:hover:not(:disabled) { background: #2563eb; }
.btn.primary:disabled { background: #93c5fd; cursor: not-allowed; }
.btn.secondary { background: #f1f5f9; color: #475569; }
.btn.secondary:hover { background: #e2e8f0; }
.empty { color: #94a3b8; font-size: 0.9rem; padding: 1rem 0; }
.error { color: #ef4444; font-size: 0.85rem; margin-bottom: 0.5rem; }
textarea { width: 100%; resize: vertical; }
</style>
