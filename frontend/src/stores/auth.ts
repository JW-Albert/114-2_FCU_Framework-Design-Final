import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userRoles = ref<string[]>(JSON.parse(localStorage.getItem('roles') ?? '[]'))

  const isAdmin = computed(() => userRoles.value.includes('admin'))

  function setToken(newToken: string, roles: string[]) {
    token.value = newToken
    userRoles.value = roles
    localStorage.setItem('token', newToken)
    localStorage.setItem('roles', JSON.stringify(roles))
  }

  function logout() {
    token.value = null
    userRoles.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('roles')
  }

  return { token, userRoles, isAdmin, setToken, logout }
})
