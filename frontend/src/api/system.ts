import axios from 'axios'

export interface SystemHealth {
  status: string
}

/**
 * 系統健康狀態（Spring Boot Actuator）。
 *
 * 直接呼叫 /actuator/health（不在 /api 之下），由 Vite proxy 轉發至後端。
 */
export const systemApi = {
  health: () =>
    axios.get<SystemHealth>('/actuator/health').then(r => r.data.status),
}
