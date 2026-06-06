import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userRoles = ref<string[]>(JSON.parse(localStorage.getItem('roles') ?? '[]'))
  const userEmail = ref<string>(localStorage.getItem('userEmail') ?? '')
  const userName = ref<string>(localStorage.getItem('userName') ?? '')

  const isAdmin = computed(() =>
    userRoles.value.some(r => r.toLowerCase() === 'admin')
  )

  function setToken(newToken: string, roles: string[], email: string, name: string) {
    token.value = newToken
    userRoles.value = roles
    userEmail.value = email
    userName.value = name
    localStorage.setItem('token', newToken)
    localStorage.setItem('roles', JSON.stringify(roles))
    localStorage.setItem('userEmail', email)
    localStorage.setItem('userName', name)
  }

  function logout() {
    token.value = null
    userRoles.value = []
    userEmail.value = ''
    userName.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('roles')
    localStorage.removeItem('userEmail')
    localStorage.removeItem('userName')
  }

  return { token, userRoles, userEmail, userName, isAdmin, setToken, logout }
})
