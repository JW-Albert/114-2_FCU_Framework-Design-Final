<template>
  <div class="page">
    <div class="header">
      <h2>稽核日誌</h2>
      <button class="btn secondary" @click="load">重新整理</button>
    </div>

    <section class="card">
      <div v-if="loading" class="empty">載入中…</div>
      <div v-else-if="items.length === 0" class="empty">目前沒有稽核紀錄</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>時間</th>
              <th>操作</th>
              <th>明細</th>
              <th>目標 ID</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="a in items" :key="a.id">
              <td class="time">{{ fmt(a.createdAt) }}</td>
              <td><span class="badge">{{ a.action }}</span></td>
              <td class="detail">{{ a.detail }}</td>
              <td class="id" :title="a.targetId ?? ''">{{ shortId(a.targetId) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { auditApi, type AuditLog } from '../api/audit'

const items = ref<AuditLog[]>([])
const loading = ref(false)

onMounted(load)

async function load() {
  loading.value = true
  try {
    items.value = await auditApi.list()
  } catch {
    items.value = []
  } finally {
    loading.value = false
  }
}

function shortId(id: string | null) {
  return id ? id.slice(0, 8) + '…' : '-'
}

function fmt(iso: string) {
  if (!iso) return '-'
  return new Date(iso).toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
  })
}
</script>

<style scoped>
.page { max-width: 1100px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; }
h2 { font-size: 1.5rem; color: #1e293b; }
.card { background: white; border-radius: 10px; padding: 1.5rem; box-shadow: 0 1px 6px rgba(0,0,0,.06); }
.empty { color: #94a3b8; font-size: 0.9rem; padding: 1.5rem 0; text-align: center; }
.table-wrap { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
th { background: #f8fafc; text-align: left; padding: 0.6rem 0.85rem; font-size: 0.8rem; color: #64748b; font-weight: 600; border-bottom: 1px solid #e2e8f0; }
td { padding: 0.65rem 0.85rem; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
tr:last-child td { border-bottom: none; }
tr:hover td { background: #f8fafc; }
.time { white-space: nowrap; color: #64748b; font-variant-numeric: tabular-nums; }
.badge { font-size: 0.75rem; font-weight: 700; padding: 0.2rem 0.6rem; border-radius: 6px; background: #eef2ff; color: #4338ca; }
.detail { color: #475569; max-width: 480px; word-break: break-word; }
.id { font-family: monospace; font-size: 0.82rem; color: #64748b; }
.btn { padding: 0.5rem 1rem; border-radius: 7px; font-size: 0.9rem; font-weight: 600; border: none; cursor: pointer; background: #f1f5f9; color: #475569; }
.btn.secondary:hover { background: #e2e8f0; }
</style>
