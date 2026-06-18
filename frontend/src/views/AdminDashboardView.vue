<template>
  <div class="dashboard">
    <h1 class="page-title">統計總覽</h1>

    <!-- KPI Cards -->
    <div class="kpi-grid" v-if="overview">
      <div class="kpi-card blue">
        <div class="kpi-icon">🚗</div>
        <div class="kpi-value">{{ overview.totalVehicles }}</div>
        <div class="kpi-label">車輛總數</div>
      </div>
      <div class="kpi-card green">
        <div class="kpi-icon">✅</div>
        <div class="kpi-value">{{ overview.availableVehicles }}</div>
        <div class="kpi-label">可用車輛</div>
      </div>
      <div class="kpi-card orange">
        <div class="kpi-icon">⏳</div>
        <div class="kpi-value">{{ overview.pendingRequests }}</div>
        <div class="kpi-label">待審核申請</div>
      </div>
      <div class="kpi-card red">
        <div class="kpi-icon">🔑</div>
        <div class="kpi-value">{{ overview.activeUses }}</div>
        <div class="kpi-label">使用中</div>
      </div>
      <div class="kpi-card purple">
        <div class="kpi-icon">👥</div>
        <div class="kpi-value">{{ overview.totalUsers }}</div>
        <div class="kpi-label">使用者總數</div>
      </div>
    </div>

    <!-- Charts row -->
    <div class="charts-grid">
      <div class="chart-card">
        <h2 class="chart-title">近 12 個月借用趨勢</h2>
        <canvas ref="monthlyChartRef" height="260"></canvas>
      </div>
      <div class="chart-card">
        <h2 class="chart-title">車輛使用率（前 10）</h2>
        <canvas ref="utilizationChartRef" height="260"></canvas>
      </div>
    </div>

    <!-- User activity table -->
    <div class="table-card">
      <h2 class="chart-title">使用者活躍度排行（前 10）</h2>
      <table class="activity-table">
        <thead>
          <tr>
            <th>排名</th>
            <th>姓名</th>
            <th>申請次數</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(user, idx) in userActivity" :key="user.userId">
            <td class="rank">{{ idx + 1 }}</td>
            <td>{{ user.name }}</td>
            <td><span class="count-badge">{{ user.requestCount }}</span></td>
          </tr>
          <tr v-if="userActivity.length === 0">
            <td colspan="3" class="empty">尚無資料</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Chart, registerables } from 'chart.js'
import { client } from '../api/client'

Chart.register(...registerables)

interface Overview {
  totalVehicles: number
  availableVehicles: number
  pendingRequests: number
  activeUses: number
  totalUsers: number
}

interface MonthlyStats {
  month: string
  count: number
}

interface VehicleUtilization {
  vehicleId: string
  plate: string
  model: string
  usageCount: number
  totalDays: number
}

interface UserActivity {
  userId: string
  name: string
  requestCount: number
}

const overview = ref<Overview | null>(null)
const userActivity = ref<UserActivity[]>([])

const monthlyChartRef = ref<HTMLCanvasElement | null>(null)
const utilizationChartRef = ref<HTMLCanvasElement | null>(null)

let monthlyChart: Chart | null = null
let utilizationChart: Chart | null = null

onMounted(async () => {
  try {
    const [overviewRes, monthlyRes, utilizationRes, activityRes] = await Promise.all([
      client.get<Overview>('/stats/overview'),
      client.get<MonthlyStats[]>('/stats/monthly'),
      client.get<VehicleUtilization[]>('/stats/vehicle-utilization'),
      client.get<UserActivity[]>('/stats/user-activity'),
    ])

    overview.value = overviewRes.data
    userActivity.value = activityRes.data

    // Monthly bar chart
    if (monthlyChartRef.value) {
      const monthly = monthlyRes.data
      monthlyChart = new Chart(monthlyChartRef.value, {
        type: 'bar',
        data: {
          labels: monthly.map(m => m.month),
          datasets: [{
            label: '借用申請數',
            data: monthly.map(m => m.count),
            backgroundColor: 'rgba(59, 130, 246, 0.7)',
            borderColor: 'rgba(59, 130, 246, 1)',
            borderWidth: 1,
            borderRadius: 4,
          }],
        },
        options: {
          responsive: true,
          plugins: {
            legend: { display: false },
          },
          scales: {
            y: {
              beginAtZero: true,
              ticks: { stepSize: 1 },
            },
          },
        },
      })
    }

    // Vehicle utilization horizontal bar chart (top 10)
    if (utilizationChartRef.value) {
      const utilization = utilizationRes.data.slice(0, 10)
      utilizationChart = new Chart(utilizationChartRef.value, {
        type: 'bar',
        data: {
          labels: utilization.map(v => `${v.plate} (${v.model})`),
          datasets: [{
            label: '使用次數',
            data: utilization.map(v => v.usageCount),
            backgroundColor: 'rgba(16, 185, 129, 0.7)',
            borderColor: 'rgba(16, 185, 129, 1)',
            borderWidth: 1,
            borderRadius: 4,
          }],
        },
        options: {
          indexAxis: 'y',
          responsive: true,
          plugins: {
            legend: { display: false },
          },
          scales: {
            x: {
              beginAtZero: true,
              ticks: { stepSize: 1 },
            },
          },
        },
      })
    }
  } catch (err) {
    console.error('Failed to load stats:', err)
  }
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 1.5rem;
}

/* KPI Cards */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.kpi-card {
  background: white;
  border-radius: 12px;
  padding: 1.25rem 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  border-top: 4px solid transparent;
}

.kpi-card.blue   { border-top-color: #3b82f6; }
.kpi-card.green  { border-top-color: #10b981; }
.kpi-card.orange { border-top-color: #f59e0b; }
.kpi-card.red    { border-top-color: #ef4444; }
.kpi-card.purple { border-top-color: #8b5cf6; }

.kpi-icon {
  font-size: 1.5rem;
}

.kpi-value {
  font-size: 2rem;
  font-weight: 800;
  color: #1e293b;
  line-height: 1;
}

.kpi-label {
  font-size: 0.8rem;
  color: #64748b;
  font-weight: 500;
  text-align: center;
}

/* Charts */
.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 1.25rem;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.chart-title {
  font-size: 1rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 1rem;
}

/* Table */
.table-card {
  background: white;
  border-radius: 12px;
  padding: 1.25rem;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.activity-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.activity-table th {
  text-align: left;
  padding: 0.6rem 1rem;
  color: #64748b;
  font-weight: 600;
  font-size: 0.8rem;
  border-bottom: 2px solid #f1f5f9;
}

.activity-table td {
  padding: 0.65rem 1rem;
  border-bottom: 1px solid #f1f5f9;
  color: #334155;
}

.activity-table tr:last-child td {
  border-bottom: none;
}

.rank {
  font-weight: 700;
  color: #94a3b8;
  width: 60px;
}

.count-badge {
  background: #eff6ff;
  color: #3b82f6;
  border-radius: 999px;
  padding: 0.15rem 0.65rem;
  font-weight: 700;
  font-size: 0.85rem;
}

.empty {
  text-align: center;
  color: #94a3b8;
  padding: 2rem;
}
</style>
