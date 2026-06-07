import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: () => import('../views/LoginView.vue'), meta: { public: true } },
  { path: '/employee/borrow', component: () => import('../views/EmployeeBorrowView.vue') },
  { path: '/admin/review', component: () => import('../views/AdminReviewView.vue') },
  { path: '/admin/vehicles', component: () => import('../views/AdminVehiclesView.vue') },
  { path: '/admin/maintenance', component: () => import('../views/AdminMaintenanceView.vue') },
  { path: '/admin/users', component: () => import('../views/AdminUserManagementView.vue'), meta: { adminOnly: true } },
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
})

export default router
