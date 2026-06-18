import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userRoles = ref<string[]>(JSON.parse(localStorage.getItem('roles') ?? '[]'))
  const userEmail = ref<string>(localStorage.getItem('userEmail') ?? '')
  const userName = ref<string>(localStorage.getItem('userName') ?? '')
  const userDepartment = ref<string>(localStorage.getItem('userDepartment') ?? '')

  const isAdmin = computed(() =>
    userRoles.value.some(r => r.toLowerCase() === 'admin')
  )

  const isManager = computed(() =>
    userRoles.value.some(r => r.toLowerCase() === 'manager')
  )

  function setToken(newToken: string, roles: string[], email: string, name: string, department?: string) {
    token.value = newToken
    userRoles.value = roles
    userEmail.value = email
    userName.value = name
    userDepartment.value = department ?? ''
    localStorage.setItem('token', newToken)
    localStorage.setItem('roles', JSON.stringify(roles))
    localStorage.setItem('userEmail', email)
    localStorage.setItem('userName', name)
    localStorage.setItem('userDepartment', department ?? '')
  }

  function logout() {
    token.value = null
    userRoles.value = []
    userEmail.value = ''
    userName.value = ''
    userDepartment.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('roles')
    localStorage.removeItem('userEmail')
    localStorage.removeItem('userName')
    localStorage.removeItem('userDepartment')
  }

  return { token, userRoles, userEmail, userName, userDepartment, isAdmin, isManager, setToken, logout }
})
