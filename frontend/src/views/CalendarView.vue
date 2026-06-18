<template>
  <div class="calendar-page">
    <div class="calendar-header">
      <h1>借用日曆</h1>
      <div class="controls">
        <label>篩選車輛：</label>
        <select v-model="selectedVehicleId" class="vehicle-filter">
          <option value="">全部車輛</option>
          <option v-for="v in vehicles" :key="v.id" :value="v.id">
            {{ v.plate }} – {{ v.model }}
          </option>
        </select>
      </div>
    </div>

    <!-- Legend -->
    <div v-if="legendVehicles.length" class="legend">
      <span
        v-for="v in legendVehicles"
        :key="v.id"
        class="legend-item"
        :style="{ background: vehicleColor(v.id) }"
      >
        {{ v.plate }}
      </span>
    </div>

    <!-- Month nav -->
    <div class="month-nav">
      <button class="nav-btn" @click="prevMonth">&#8249;</button>
      <span class="month-label">{{ monthLabel }}</span>
      <button class="nav-btn" @click="nextMonth">&#8250;</button>
    </div>

    <!-- Calendar grid -->
    <div class="calendar-grid">
      <div class="day-header" v-for="d in weekDays" :key="d">{{ d }}</div>

      <div
        v-for="cell in calendarDays"
        :key="cell.date.toISOString()"
        class="day-cell"
        :class="{
          'other-month': !cell.isCurrentMonth,
          'today': cell.isToday,
        }"
      >
        <div class="day-number">{{ cell.date.getDate() }}</div>
        <div class="events-list">
          <div
            v-for="ev in cell.events"
            :key="ev.id"
            class="event-pill"
            :style="{ background: vehicleColor(ev.vehicleId) }"
            :title="eventTooltip(ev)"
            @click.stop="selectEvent(ev)"
          >
            {{ vehiclePlate(ev.vehicleId) }}
          </div>
        </div>
      </div>
    </div>

    <!-- Event detail popup -->
    <transition name="fade">
      <div v-if="selectedEvent" class="popup-overlay" @click="selectedEvent = null">
        <div class="popup" @click.stop>
          <button class="popup-close" @click="selectedEvent = null">✕</button>
          <h3>借用詳情</h3>
          <table class="popup-table">
            <tr>
              <th>申請人</th>
              <td>{{ userName(selectedEvent.userId) }}</td>
            </tr>
            <tr>
              <th>車輛</th>
              <td>{{ vehiclePlate(selectedEvent.vehicleId) }}</td>
            </tr>
            <tr>
              <th>用途</th>
              <td>{{ selectedEvent.purpose }}</td>
            </tr>
            <tr>
              <th>開始時段</th>
              <td>{{ formatTime(selectedEvent.periodStart) }}</td>
            </tr>
            <tr>
              <th>結束時段</th>
              <td>{{ formatTime(selectedEvent.periodEnd) }}</td>
            </tr>
            <tr>
              <th>狀態</th>
              <td>
                <span class="state-badge" :class="selectedEvent.state.toLowerCase()">
                  {{ stateLabel(selectedEvent.state) }}
                </span>
              </td>
            </tr>
          </table>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { borrowingsApi, type BorrowingRequest } from '../api/borrowings'
import { vehiclesApi, type Vehicle } from '../api/vehicles'
import { usersApi, type UserRecord } from '../api/users'

const currentDate = ref(new Date())
const borrowings = ref<BorrowingRequest[]>([])
const vehicles = ref<Vehicle[]>([])
const users = ref<UserRecord[]>([])
const selectedVehicleId = ref<string>('')
const selectedEvent = ref<BorrowingRequest | null>(null)

const weekDays = ['一', '二', '三', '四', '五', '六', '日']

// ── helpers ──────────────────────────────────────────────

function vehicleColor(vehicleId: string): string {
  let hash = 0
  for (let i = 0; i < vehicleId.length; i++) {
    hash = (hash * 31 + vehicleId.charCodeAt(i)) >>> 0
  }
  const hue = hash % 360
  return `hsl(${hue}, 65%, 58%)`
}

function vehiclePlate(vehicleId: string): string {
  return vehicles.value.find(v => v.id === vehicleId)?.plate ?? vehicleId.slice(0, 8)
}

function userName(userId: string): string {
  return users.value.find(u => u.id === userId)?.name ?? userId.slice(0, 8)
}

function formatTime(iso: string): string {
  const d = new Date(iso)
  return d.toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}

function stateLabel(state: string): string {
  const map: Record<string, string> = {
    PENDING: '待審核',
    APPROVED: '已核准',
    IN_USE: '使用中',
    RETURNED: '已還車',
    REJECTED: '已拒絕',
  }
  return map[state] ?? state
}

function eventTooltip(ev: BorrowingRequest): string {
  return `${vehiclePlate(ev.vehicleId)} | ${userName(ev.userId)} | ${ev.purpose} | ${stateLabel(ev.state)}`
}

function selectEvent(ev: BorrowingRequest) {
  selectedEvent.value = ev
}

// ── computed ─────────────────────────────────────────────

const monthLabel = computed(() => {
  return currentDate.value.toLocaleString('zh-TW', { year: 'numeric', month: 'long' })
})

const legendVehicles = computed(() => {
  const ids = new Set(filteredBorrowings.value.map(b => b.vehicleId))
  return vehicles.value.filter(v => ids.has(v.id))
})

const filteredBorrowings = computed(() => {
  if (!selectedVehicleId.value) return borrowings.value
  return borrowings.value.filter(b => b.vehicleId === selectedVehicleId.value)
})

const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()

  // First day of the month
  const firstDay = new Date(year, month, 1)
  // Day of week for first day (0=Sun..6=Sat), convert to Mon=0..Sun=6
  let startDow = firstDay.getDay() - 1
  if (startDow < 0) startDow = 6

  const today = new Date()
  const todayStr = today.toDateString()

  const cells: Array<{
    date: Date
    isCurrentMonth: boolean
    isToday: boolean
    events: BorrowingRequest[]
  }> = []

  // Start from Monday of the week containing the 1st
  const startDate = new Date(firstDay)
  startDate.setDate(startDate.getDate() - startDow)

  for (let i = 0; i < 42; i++) {
    const cellDate = new Date(startDate)
    cellDate.setDate(startDate.getDate() + i)

    const isCurrentMonth = cellDate.getMonth() === month
    const isToday = cellDate.toDateString() === todayStr

    // Events overlapping this day
    const dayStart = new Date(cellDate)
    dayStart.setHours(0, 0, 0, 0)
    const dayEnd = new Date(cellDate)
    dayEnd.setHours(23, 59, 59, 999)

    const events = filteredBorrowings.value.filter(b => {
      const pStart = new Date(b.periodStart)
      const pEnd = new Date(b.periodEnd)
      return pStart <= dayEnd && pEnd >= dayStart
    })

    cells.push({ date: cellDate, isCurrentMonth, isToday, events })
  }

  return cells
})

// ── data fetching ─────────────────────────────────────────

async function fetchMonthData() {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const start = new Date(year, month, 1).toISOString()
  const end = new Date(year, month + 1, 0, 23, 59, 59, 999).toISOString()

  try {
    const res = await borrowingsApi.listCalendar(start, end)
    borrowings.value = res.data
  } catch (e) {
    console.error('Failed to fetch calendar data', e)
    borrowings.value = []
  }
}

function prevMonth() {
  const d = new Date(currentDate.value)
  d.setMonth(d.getMonth() - 1)
  currentDate.value = d
}

function nextMonth() {
  const d = new Date(currentDate.value)
  d.setMonth(d.getMonth() + 1)
  currentDate.value = d
}

watch(currentDate, fetchMonthData)

onMounted(async () => {
  try {
    const [vRes, uRes] = await Promise.all([
      vehiclesApi.listAll(),
      usersApi.listAll(),
    ])
    vehicles.value = vRes.data
    users.value = uRes.data
  } catch (e) {
    console.error('Failed to load vehicles/users', e)
  }
  await fetchMonthData()
})
</script>

<style scoped>
.calendar-page {
  max-width: 1100px;
  margin: 0 auto;
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.calendar-header h1 {
  font-size: 1.4rem;
  font-weight: 700;
  color: #1e293b;
}

.controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.vehicle-filter {
  padding: 0.35rem 0.6rem;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 0.9rem;
  background: white;
}

/* Legend */
.legend {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.legend-item {
  padding: 0.2rem 0.7rem;
  border-radius: 999px;
  font-size: 0.78rem;
  color: white;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0,0,0,0.3);
}

/* Month nav */
.month-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.5rem;
  margin-bottom: 1rem;
}

.nav-btn {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  width: 36px;
  height: 36px;
  font-size: 1.4rem;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
}

.nav-btn:hover {
  background: #e2e8f0;
}

.month-label {
  font-size: 1.15rem;
  font-weight: 700;
  min-width: 140px;
  text-align: center;
  color: #1e293b;
}

/* Calendar grid */
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  border-top: 1px solid #e2e8f0;
  border-left: 1px solid #e2e8f0;
}

.day-header {
  text-align: center;
  padding: 0.4rem;
  font-weight: 600;
  font-size: 0.82rem;
  color: #64748b;
  background: #f8fafc;
  border-right: 1px solid #e2e8f0;
  border-bottom: 1px solid #e2e8f0;
}

.day-cell {
  min-height: 100px;
  padding: 0.35rem;
  border-right: 1px solid #e2e8f0;
  border-bottom: 1px solid #e2e8f0;
  background: white;
  vertical-align: top;
  overflow: hidden;
}

.day-cell.other-month {
  background: #f8fafc;
}

.day-cell.other-month .day-number {
  color: #cbd5e1;
}

.day-cell.today {
  background: #eff6ff;
}

.day-cell.today .day-number {
  background: #3b82f6;
  color: white;
  border-radius: 50%;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.day-number {
  font-size: 0.8rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.25rem;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.event-pill {
  font-size: 0.72rem;
  padding: 1px 5px;
  border-radius: 4px;
  color: white;
  font-weight: 600;
  text-shadow: 0 1px 1px rgba(0,0,0,0.25);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  transition: opacity 0.15s;
}

.event-pill:hover {
  opacity: 0.85;
}

/* Popup */
.popup-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.popup {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  min-width: 320px;
  max-width: 460px;
  width: 90%;
  position: relative;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
}

.popup h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 1rem;
}

.popup-close {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  background: transparent;
  border: none;
  font-size: 1rem;
  cursor: pointer;
  color: #94a3b8;
  line-height: 1;
  padding: 0.2rem;
}

.popup-close:hover {
  color: #1e293b;
}

.popup-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.popup-table th {
  text-align: left;
  padding: 0.4rem 0.6rem 0.4rem 0;
  color: #64748b;
  font-weight: 600;
  width: 80px;
  vertical-align: top;
}

.popup-table td {
  padding: 0.4rem 0;
  color: #1e293b;
}

.state-badge {
  display: inline-block;
  padding: 0.15rem 0.55rem;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 600;
}

.state-badge.pending   { background: #fef9c3; color: #854d0e; }
.state-badge.approved  { background: #dcfce7; color: #166534; }
.state-badge.in_use    { background: #dbeafe; color: #1e40af; }
.state-badge.returned  { background: #f1f5f9; color: #475569; }
.state-badge.rejected  { background: #fee2e2; color: #991b1b; }

/* Transition */
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

@media (max-width: 600px) {
  .calendar-grid {
    font-size: 0.75rem;
  }
  .day-cell {
    min-height: 60px;
    padding: 0.2rem;
  }
  .event-pill {
    font-size: 0.65rem;
  }
}
</style>
