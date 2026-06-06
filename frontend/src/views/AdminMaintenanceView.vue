<template>
  <div class="page">
    <h2>保養管理</h2>

    <!-- 車輛選擇 -->
    <section class="card">
      <h3>選擇車輛</h3>
      <div class="vehicle-selector">
        <button
          v-for="v in vehicles"
          :key="v.id"
          :class="['vehicle-btn', { active: selectedVehicleId === v.id }]"
          @click="selectVehicle(v.id)"
        >
          <strong>{{ v.plate }}</strong>
          <span>{{ v.model }}</span>
        </button>
      </div>
    </section>

    <template v-if="selectedVehicleId">
      <!-- 新增保養記錄 -->
      <section class="card">
        <h3>新增保養記錄</h3>
        <div class="form-grid">
          <div class="field">
            <label>保養日期</label>
            <input v-model="form.date" type="date" />
          </div>
          <div class="field">
            <label>費用（元）</label>
            <input v-model.number="form.cost" type="number" min="0" placeholder="0" />
          </div>
          <div class="field">
            <label>下次保養日期（選填）</label>
            <input v-model="form.nextDueDate" type="date" />
          </div>
          <div class="field">
            <label>下次保養里程（選填）</label>
            <input v-model.number="form.nextDueKm" type="number" min="0" placeholder="km" />
          </div>
          <div class="field full">
            <label>保養項目（每行一項）</label>
            <textarea v-model="itemsText" rows="3" placeholder="機油更換&#10;輪胎檢查&#10;煞車保養"></textarea>
          </div>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <button class="btn primary" @click="addRecord" :disabled="saving">
          {{ saving ? '儲存中…' : '新增記錄' }}
        </button>
      </section>

      <!-- 保養記錄列表 -->
      <section class="card">
        <div class="section-header">
          <h3>保養記錄</h3>
          <button class="btn secondary" @click="loadRecords">重新整理</button>
        </div>

        <div v-if="records.length === 0" class="empty">此車輛尚無保養記錄</div>
        <div v-else class="record-list">
          <div v-for="r in records" :key="r.id" class="record-card" :class="{ due: isDue(r) }">
            <div class="rec-header">
              <span class="rec-date">{{ r.date }}</span>
              <span class="rec-cost">NT$ {{ r.cost?.toLocaleString() }}</span>
              <span v-if="isDue(r)" class="due-badge">⚠ 應保養</span>
              <button class="btn-icon del" @click="deleteRecord(r.id)" title="刪除">🗑️</button>
            </div>
            <div class="rec-items">
              <span v-for="item in r.items" :key="item" class="item-tag">{{ item }}</span>
            </div>
            <div class="rec-next">
              <span v-if="r.nextDueDate">下次：{{ r.nextDueDate }}</span>
              <span v-if="r.nextDueKm">（{{ r.nextDueKm.toLocaleString() }} km）</span>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { vehiclesApi, type Vehicle } from '../api/vehicles'
import { maintenanceApi, type MaintenanceRecord } from '../api/maintenance'

const vehicles = ref<Vehicle[]>([])
const selectedVehicleId = ref<string | null>(null)
const records = ref<MaintenanceRecord[]>([])
const saving = ref(false)
const formError = ref('')

const form = ref({ date: '', cost: 0, nextDueDate: '', nextDueKm: undefined as number | undefined })
const itemsText = ref('')

onMounted(async () => {
  try {
    const { data } = await vehiclesApi.listAll()
    vehicles.value = data
  } catch {}
})

async function selectVehicle(id: string) {
  selectedVehicleId.value = id
  await loadRecords()
}

async function loadRecords() {
  if (!selectedVehicleId.value) return
  try {
    const { data } = await maintenanceApi.getByVehicle(selectedVehicleId.value)
    records.value = data.sort((a, b) => b.date.localeCompare(a.date))
  } catch {}
}

async function addRecord() {
  if (!form.value.date || !itemsText.value.trim()) {
    formError.value = '請填寫保養日期及保養項目'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    const items = itemsText.value.split('\n').map(s => s.trim()).filter(Boolean)
    await maintenanceApi.add({
      vehicleId: selectedVehicleId.value!,
      date: form.value.date,
      items,
      cost: form.value.cost,
      nextDueDate: form.value.nextDueDate || undefined,
      nextDueKm: form.value.nextDueKm || undefined,
    })
    form.value = { date: '', cost: 0, nextDueDate: '', nextDueKm: undefined }
    itemsText.value = ''
    await loadRecords()
  } catch (e: any) {
    formError.value = e.response?.data?.error ?? '新增失敗'
  } finally {
    saving.value = false
  }
}

async function deleteRecord(id: string) {
  if (!confirm('確定刪除此保養記錄？')) return
  try {
    await maintenanceApi.delete(id)
    await loadRecords()
  } catch {}
}

function isDue(r: MaintenanceRecord) {
  if (r.nextDueDate) {
    const today = new Date().toISOString().slice(0, 10)
    if (r.nextDueDate <= today) return true
  }
  return false
}
</script>

<style scoped>
.page { max-width: 960px; margin: 0 auto; }
h2 { font-size: 1.5rem; margin-bottom: 1.25rem; color: #1e293b; }
.card { background: white; border-radius: 10px; padding: 1.5rem; box-shadow: 0 1px 6px rgba(0,0,0,.06); margin-bottom: 1.25rem; }
h3 { font-size: 1.05rem; margin-bottom: 1rem; color: #334155; }
.vehicle-selector { display: flex; gap: 0.75rem; flex-wrap: wrap; }
.vehicle-btn { display: flex; flex-direction: column; gap: 0.15rem; padding: 0.65rem 1rem; border: 2px solid #e2e8f0; border-radius: 8px; cursor: pointer; background: white; transition: all .15s; text-align: left; }
.vehicle-btn:hover { border-color: #93c5fd; }
.vehicle-btn.active { border-color: #3b82f6; background: #eff6ff; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.field { display: flex; flex-direction: column; gap: 0.3rem; }
.field.full { grid-column: 1 / -1; }
label { font-size: 0.8rem; font-weight: 600; color: #64748b; }
input, textarea { padding: 0.55rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 7px; font-size: 0.9rem; }
input:focus, textarea:focus { outline: none; border-color: #3b82f6; }
textarea { resize: vertical; width: 100%; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.record-list { display: flex; flex-direction: column; gap: 0.75rem; }
.record-card { border: 1px solid #e2e8f0; border-radius: 8px; padding: 1rem 1.25rem; }
.record-card.due { border-color: #f97316; background: #fff7ed; }
.rec-header { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem; }
.rec-date { font-weight: 700; font-size: 0.95rem; }
.rec-cost { color: #475569; font-size: 0.9rem; margin-left: auto; }
.due-badge { background: #fed7aa; color: #c2410c; font-size: 0.75rem; padding: 0.15rem 0.5rem; border-radius: 999px; font-weight: 600; }
.rec-items { display: flex; flex-wrap: wrap; gap: 0.4rem; margin-bottom: 0.4rem; }
.item-tag { background: #f1f5f9; color: #475569; font-size: 0.78rem; padding: 0.2rem 0.6rem; border-radius: 999px; }
.rec-next { font-size: 0.8rem; color: #94a3b8; }
.btn { padding: 0.55rem 1.1rem; border-radius: 7px; font-size: 0.9rem; font-weight: 600; border: none; cursor: pointer; transition: all .15s; margin-top: 0.75rem; }
.btn.primary { background: #3b82f6; color: white; }
.btn.primary:hover:not(:disabled) { background: #2563eb; }
.btn.primary:disabled { background: #93c5fd; cursor: not-allowed; }
.btn.secondary { background: #f1f5f9; color: #475569; margin-top: 0; }
.btn.secondary:hover { background: #e2e8f0; }
.btn-icon { background: none; border: none; cursor: pointer; font-size: 1rem; padding: 0.2rem 0.4rem; border-radius: 4px; }
.btn-icon.del:hover { background: #fee2e2; }
.empty { color: #94a3b8; font-size: 0.9rem; padding: 1rem 0; }
.error { color: #ef4444; font-size: 0.85rem; margin-top: 0.5rem; }
</style>
