<template>
  <div class="page">
    <h2>借車申請審核</h2>

    <div class="tabs">
      <button :class="['tab', { active: tab === 'pending' }]" @click="tab = 'pending'">
        待審核 <span class="count">{{ pendingList.length }}</span>
      </button>
      <button :class="['tab', { active: tab === 'all' }]" @click="tab = 'all'; loadAll()">
        全部申請
      </button>
    </div>

    <!-- 待審核列表 -->
    <section v-if="tab === 'pending'" class="card">
      <div class="section-header">
        <h3>待審核申請</h3>
        <button class="btn secondary" @click="loadPending">重新整理</button>
      </div>

      <div v-if="pendingList.length === 0" class="empty">目前無待審核申請</div>
      <div v-else class="request-list">
        <div v-for="r in pendingList" :key="r.id" class="request-card">
          <div class="req-header">
            <span class="req-vehicle">🚗 {{ vehicleMap[r.vehicleId] ?? r.vehicleId.slice(0,8) }}</span>
            <span class="req-period">{{ fmt(r.periodStart) }} ～ {{ fmt(r.periodEnd) }}</span>
          </div>
          <p class="req-purpose">{{ r.purpose }}</p>
          <p class="req-meta">申請時間：{{ fmt(r.createdAt) }}</p>

          <div class="review-actions">
            <input
              v-model="noteMap[r.id]"
              placeholder="審核備注（選填）"
              class="note-input"
            />
            <button class="btn approve" @click="approve(r.id)" :disabled="actionLoading[r.id]">
              核准
            </button>
            <button class="btn reject" @click="reject(r.id)" :disabled="actionLoading[r.id]">
              拒絕
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 全部申請 -->
    <section v-if="tab === 'all'" class="card">
      <h3>全部借車申請</h3>
      <div v-if="allRequests.length === 0" class="empty">尚無申請記錄</div>
      <table v-else class="table">
        <thead>
          <tr>
            <th>車牌</th>
            <th>開始</th>
            <th>結束</th>
            <th>目的</th>
            <th>狀態</th>
            <th>備注</th>
            <th>申請時間</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in allRequests" :key="r.id">
            <td>{{ vehicleMap[r.vehicleId] ?? r.vehicleId.slice(0,8) }}</td>
            <td>{{ fmt(r.periodStart) }}</td>
            <td>{{ fmt(r.periodEnd) }}</td>
            <td>{{ r.purpose }}</td>
            <td><span :class="['badge', stateClass(r.state)]">{{ stateLabel(r.state) }}</span></td>
            <td>{{ r.reviewNote ?? '—' }}</td>
            <td>{{ fmt(r.createdAt) }}</td>
            <td>
              <div v-if="r.state === 'IN_USE'" class="complete-inline">
                <input
                  v-model.number="mileageMap[r.id]"
                  type="number"
                  min="0"
                  placeholder="結束里程 (km)"
                  class="mileage-input"
                />
                <button
                  class="btn complete"
                  @click="complete(r.id)"
                  :disabled="actionLoading[r.id] || !mileageMap[r.id]"
                >
                  完成還車
                </button>
              </div>
              <span v-else-if="r.endMileage != null" class="mileage-info">{{ r.endMileage }} km</span>
              <span v-else>—</span>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { vehiclesApi } from '../api/vehicles'
import { borrowingsApi, type BorrowingRequest } from '../api/borrowings'

const tab = ref<'pending' | 'all'>('pending')
const pendingList = ref<BorrowingRequest[]>([])
const allRequests = ref<BorrowingRequest[]>([])
const vehicleMap = ref<Record<string, string>>({})
const noteMap = ref<Record<string, string>>({})
const mileageMap = ref<Record<string, number>>({})
const actionLoading = ref<Record<string, boolean>>({})

onMounted(async () => {
  await Promise.all([loadPending(), loadVehicleMap()])
})

async function loadVehicleMap() {
  try {
    const { data } = await vehiclesApi.listAll()
    data.forEach(v => { vehicleMap.value[v.id] = v.plate })
  } catch {}
}

async function loadPending() {
  try {
    const { data } = await borrowingsApi.listPending()
    pendingList.value = data
  } catch {}
}

async function loadAll() {
  try {
    const { data } = await borrowingsApi.listAll()
    allRequests.value = data.sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )
  } catch {}
}

async function approve(id: string) {
  actionLoading.value[id] = true
  try {
    await borrowingsApi.approve(id, noteMap.value[id])
    await loadPending()
  } finally {
    actionLoading.value[id] = false
  }
}

async function reject(id: string) {
  actionLoading.value[id] = true
  try {
    await borrowingsApi.reject(id, noteMap.value[id])
    await loadPending()
  } finally {
    actionLoading.value[id] = false
  }
}

async function complete(id: string) {
  const endMileage = mileageMap.value[id]
  if (!endMileage && endMileage !== 0) return
  actionLoading.value[id] = true
  try {
    await borrowingsApi.complete(id, endMileage)
    await loadAll()
  } catch (e: any) {
    alert(e.response?.data?.message ?? '還車失敗')
  } finally {
    actionLoading.value[id] = false
  }
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
.tabs { display: flex; gap: 0.5rem; margin-bottom: 1rem; }
.tab { padding: 0.5rem 1.1rem; border: 1px solid #e2e8f0; border-radius: 7px; background: white; cursor: pointer; font-size: 0.9rem; color: #64748b; display: flex; align-items: center; gap: 0.4rem; }
.tab.active { background: #3b82f6; color: white; border-color: #3b82f6; }
.count { background: rgba(255,255,255,.25); border-radius: 999px; padding: 0 0.45rem; font-size: 0.78rem; }
.tab:not(.active) .count { background: #e2e8f0; color: #475569; }
.card { background: white; border-radius: 10px; padding: 1.5rem; box-shadow: 0 1px 6px rgba(0,0,0,.06); margin-bottom: 1.25rem; }
h3 { font-size: 1.05rem; margin-bottom: 1rem; color: #334155; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.request-list { display: flex; flex-direction: column; gap: 1rem; }
.request-card { border: 1px solid #e2e8f0; border-radius: 8px; padding: 1rem 1.25rem; }
.req-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.4rem; }
.req-vehicle { font-weight: 700; font-size: 0.95rem; }
.req-period { font-size: 0.82rem; color: #64748b; }
.req-purpose { color: #475569; font-size: 0.9rem; margin-bottom: 0.25rem; }
.req-meta { font-size: 0.78rem; color: #94a3b8; margin-bottom: 0.75rem; }
.review-actions { display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }
.note-input { flex: 1; min-width: 180px; padding: 0.45rem 0.7rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.85rem; }
.note-input:focus { outline: none; border-color: #3b82f6; }
.btn { padding: 0.45rem 1rem; border-radius: 6px; font-size: 0.85rem; font-weight: 600; border: none; cursor: pointer; transition: all .15s; }
.btn.approve { background: #dcfce7; color: #15803d; }
.btn.approve:hover:not(:disabled) { background: #bbf7d0; }
.btn.reject { background: #fee2e2; color: #b91c1c; }
.btn.reject:hover:not(:disabled) { background: #fecaca; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn.secondary { background: #f1f5f9; color: #475569; }
.btn.secondary:hover { background: #e2e8f0; }
.table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
.table th { background: #f8fafc; padding: 0.6rem 0.75rem; text-align: left; font-size: 0.8rem; color: #64748b; border-bottom: 1px solid #e2e8f0; }
.table td { padding: 0.65rem 0.75rem; border-bottom: 1px solid #f1f5f9; }
.badge { padding: 0.2rem 0.6rem; border-radius: 999px; font-size: 0.75rem; font-weight: 600; }
.badge.pending { background: #fef9c3; color: #a16207; }
.badge.approved { background: #dcfce7; color: #15803d; }
.badge.rejected { background: #fee2e2; color: #b91c1c; }
.badge.inuse { background: #dbeafe; color: #1d4ed8; }
.badge.returned { background: #f1f5f9; color: #475569; }
.empty { color: #94a3b8; font-size: 0.9rem; padding: 1rem 0; }
.complete-inline { display: flex; gap: 0.4rem; align-items: center; }
.mileage-input { width: 120px; padding: 0.35rem 0.55rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.82rem; }
.mileage-input:focus { outline: none; border-color: #3b82f6; }
.btn.complete { background: #ede9fe; color: #6d28d9; }
.btn.complete:hover:not(:disabled) { background: #ddd6fe; }
.mileage-info { font-size: 0.82rem; color: #475569; }
</style>
