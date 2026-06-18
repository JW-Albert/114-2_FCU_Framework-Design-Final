import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: () => import('../views/LoginView.vue'), meta: { public: true } },
  { path: '/employee/borrow', component: () => import('../views/EmployeeBorrowView.vue') },
  { path: '/admin/review', component: () => import('../views/AdminReviewView.vue'), meta: { approverOnly: true } },
  { path: '/admin/vehicles', component: () => import('../views/AdminVehiclesView.vue') },
  { path: '/admin/maintenance', component: () => import('../views/AdminMaintenanceView.vue') },
  { path: '/admin/users', component: () => import('../views/AdminUserManagementView.vue'), meta: { adminOnly: true } },
  { path: '/admin/violations', component: () => import('../views/AdminViolationsView.vue'), meta: { requiresAuth: true, adminOnly: true } },
  { path: '/admin/calendar', component: () => import('../views/CalendarView.vue'), meta: { requiresAuth: true } },
  { path: '/admin/dashboard', component: () => import('../views/AdminDashboardView.vue'), meta: { requiresAuth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.token) {
    return '/login'
  }
  if (to.meta.adminOnly && !auth.isAdmin) {
    return '/login'
  }
  if (to.meta.approverOnly && !auth.isAdmin && !auth.isManager) {
    return '/login'
  }
})

export default router
