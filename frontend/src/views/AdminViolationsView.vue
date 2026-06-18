<template>
  <div class="page">
    <h2>違規記錄</h2>

    <!-- 手動登錄違規 -->
    <section class="card">
      <div class="section-header">
        <h3>手動登錄違規</h3>
      </div>
      <form class="violation-form" @submit.prevent="submitViolation">
        <div class="form-row">
          <label>借用記錄</label>
          <select v-model="form.borrowingId" required>
            <option value="" disabled>請選擇借用記錄…</option>
            <option v-for="b in borrowings" :key="b.id" :value="b.id">
              {{ borrowingLabel(b) }}
            </option>
          </select>
        </div>
        <div class="form-row">
          <label>違規類型</label>
          <select v-model="form.type" required>
            <option v-for="t in VIOLATION_TYPES" :key="t.value" :value="t.value">
              {{ t.label }}
            </option>
          </select>
        </div>
        <div class="form-row">
          <label>違規描述</label>
          <textarea v-model="form.description" rows="2" required
                    placeholder="請描述違規情形…"></textarea>
        </div>
        <div class="form-actions">
          <button type="submit" class="btn primary" :disabled="submitting">
            {{ submitting ? '登錄中…' : '登錄違規' }}
          </button>
          <span v-if="formMsg" class="form-msg" :class="formMsgType">{{ formMsg }}</span>
        </div>
      </form>
      <p v-if="borrowings.length === 0" class="hint">
        尚無任何借用記錄，需先有借車申請才能登錄違規。
      </p>
    </section>

    <!-- 篩選 -->
    <section class="card">
      <div class="filter-row">
        <label>依使用者篩選</label>
        <select v-model="filterUserId" @change="load">
          <option value="">全部使用者</option>
          <option v-for="uid in uniqueUserIds" :key="uid" :value="uid">
            {{ uid }}
          </option>
        </select>
        <button class="btn secondary" @click="resetFilter">清除篩選</button>
      </div>
    </section>

    <!-- 違規記錄列表 -->
    <section class="card">
      <div class="section-header">
        <h3>違規記錄列表</h3>
        <button class="btn secondary" @click="load">重新整理</button>
      </div>

      <div v-if="loading" class="empty">載入中…</div>
      <div v-else-if="filtered.length === 0" class="empty">目前無違規記錄</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>類型</th>
              <th>使用者 ID</th>
              <th>車輛</th>
              <th>借用申請 ID</th>
              <th>描述</th>
              <th>建立時間</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="v in filtered" :key="v.id">
              <td>
                <span class="badge" :class="badgeClass(v.type)">{{ violationTypeLabel(v.type) }}</span>
              </td>
              <td class="id-cell" :title="v.userId">{{ shortId(v.userId) }}</td>
              <td class="id-cell" :title="v.vehicleId">{{ vehicleLabel(v.vehicleId) }}</td>
              <td class="id-cell" :title="v.borrowingId">{{ shortId(v.borrowingId) }}</td>
              <td class="desc-cell">{{ v.description }}</td>
              <td class="date-cell">{{ formatDate(v.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { violationsApi, violationTypeLabel, VIOLATION_TYPES, type Violation } from '../api/violations'
import { borrowingsApi, type BorrowingRequest } from '../api/borrowings'
import { vehiclesApi, type Vehicle } from '../api/vehicles'

const violations = ref<Violation[]>([])
const borrowings = ref<BorrowingRequest[]>([])
const vehicleMap = ref<Map<string, Vehicle>>(new Map())
const filterUserId = ref('')
const loading = ref(false)

const form = ref({ borrowingId: '', type: 'OVERDUE', description: '' })
const submitting = ref(false)
const formMsg = ref('')
const formMsgType = ref<'success' | 'error'>('success')

const uniqueUserIds = computed(() => {
  return [...new Set(violations.value.map(v => v.userId))]
})

const filtered = computed(() => {
  if (!filterUserId.value) return violations.value
  return violations.value.filter(v => v.userId === filterUserId.value)
})

onMounted(() => {
  load()
  loadFormData()
})

async function load() {
  loading.value = true
  try {
    violations.value = await violationsApi.listAll()
  } catch {
    violations.value = []
  } finally {
    loading.value = false
  }
}

async function loadFormData() {
  try {
    const [bRes, vRes] = await Promise.all([
      borrowingsApi.listAll(),
      vehiclesApi.listAll(),
    ])
    // 較新的借用記錄排前面，方便登錄
    borrowings.value = [...bRes.data].sort((a, b) =>
      b.createdAt.localeCompare(a.createdAt))
    vehicleMap.value = new Map(vRes.data.map(v => [v.id, v] as [string, Vehicle]))
  } catch {
    borrowings.value = []
  }
}

async function submitViolation() {
  if (!form.value.borrowingId || !form.value.type || !form.value.description.trim()) return
  submitting.value = true
  formMsg.value = ''
  try {
    await violationsApi.create(
      form.value.borrowingId, form.value.type, form.value.description.trim())
    formMsg.value = '✓ 違規已登錄'
    formMsgType.value = 'success'
    form.value = { borrowingId: '', type: 'OVERDUE', description: '' }
    await load()
  } catch (e: any) {
    formMsg.value = e?.response?.data?.message ?? '登錄失敗，請稍後再試'
    formMsgType.value = 'error'
  } finally {
    submitting.value = false
  }
}

function resetFilter() {
  filterUserId.value = ''
  load()
}

function borrowingLabel(b: BorrowingRequest): string {
  const v = vehicleMap.value.get(b.vehicleId)
  const vehicle = v ? `${v.plate} ${v.model}` : shortId(b.vehicleId)
  return `${vehicle}｜${b.purpose}｜${fmtShort(b.periodStart)}～${fmtShort(b.periodEnd)}｜${stateLabel(b.state)}`
}

function vehicleLabel(vehicleId: string): string {
  const v = vehicleMap.value.get(vehicleId)
  return v ? v.plate : shortId(vehicleId)
}

function stateLabel(state: string): string {
  const map: Record<string, string> = {
    PENDING: '待審核', APPROVED: '已核准', IN_USE: '使用中',
    RETURNED: '已還車', REJECTED: '已拒絕',
  }
  return map[state] ?? state
}

function badgeClass(type: string) {
  return type === 'OVERDUE' ? 'overdue' : 'default'
}

function shortId(id: string) {
  return id ? id.slice(0, 8) + '…' : '-'
}

function fmtShort(iso: string) {
  if (!iso) return '-'
  const d = new Date(iso)
  return d.toLocaleString('zh-TW', {
    month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit',
  })
}

function formatDate(iso: string) {
  if (!iso) return '-'
  const d = new Date(iso)
  return d.toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}
</script>

<style scoped>
.page { max-width: 1100px; margin: 0 auto; }
h2 { font-size: 1.5rem; margin-bottom: 1.25rem; color: #1e293b; }
.card { background: white; border-radius: 10px; padding: 1.5rem; box-shadow: 0 1px 6px rgba(0,0,0,.06); margin-bottom: 1.25rem; }
h3 { font-size: 1.05rem; margin-bottom: 1rem; color: #334155; }

/* 手動登錄表單 */
.violation-form { display: flex; flex-direction: column; gap: 0.9rem; }
.form-row { display: flex; flex-direction: column; gap: 0.35rem; }
.form-row label { font-size: 0.82rem; font-weight: 600; color: #64748b; }
.violation-form select,
.violation-form textarea { padding: 0.5rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 7px; font-size: 0.9rem; font-family: inherit; width: 100%; }
.violation-form select:focus,
.violation-form textarea:focus { outline: none; border-color: #3b82f6; }
.violation-form textarea { resize: vertical; }
.form-actions { display: flex; align-items: center; gap: 0.85rem; }
.form-msg { font-size: 0.85rem; font-weight: 600; }
.form-msg.success { color: #15803d; }
.form-msg.error { color: #b91c1c; }
.hint { margin-top: 0.85rem; font-size: 0.82rem; color: #94a3b8; }

.filter-row { display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap; }
.filter-row label { font-size: 0.85rem; font-weight: 600; color: #64748b; }
select { padding: 0.45rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 7px; font-size: 0.9rem; min-width: 240px; }
select:focus { outline: none; border-color: #3b82f6; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.table-wrap { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
th { background: #f8fafc; text-align: left; padding: 0.6rem 0.85rem; font-size: 0.8rem; color: #64748b; font-weight: 600; border-bottom: 1px solid #e2e8f0; }
td { padding: 0.65rem 0.85rem; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
tr:last-child td { border-bottom: none; }
tr:hover td { background: #f8fafc; }
.id-cell { font-family: monospace; font-size: 0.82rem; color: #64748b; cursor: default; }
.desc-cell { max-width: 320px; word-break: break-word; color: #475569; }
.date-cell { white-space: nowrap; color: #64748b; }
.badge { font-size: 0.75rem; font-weight: 700; padding: 0.2rem 0.6rem; border-radius: 999px; }
.badge.overdue { background: #fee2e2; color: #b91c1c; }
.badge.default { background: #f1f5f9; color: #475569; }
.btn { padding: 0.5rem 1rem; border-radius: 7px; font-size: 0.9rem; font-weight: 600; border: none; cursor: pointer; transition: all .15s; }
.btn.primary { background: #3b82f6; color: white; }
.btn.primary:hover { background: #2563eb; }
.btn.primary:disabled { background: #93c5fd; cursor: not-allowed; }
.btn.secondary { background: #f1f5f9; color: #475569; }
.btn.secondary:hover { background: #e2e8f0; }
.empty { color: #94a3b8; font-size: 0.9rem; padding: 1rem 0; }
</style>
