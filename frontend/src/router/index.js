import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', permission: 'system:user' }
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('@/views/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', permission: 'system:role' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查登录状态 + 权限
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 免登录页面直接放行
  if (to.meta.noAuth) {
    next()
    return
  }

  // 未登录 → 跳登录页
  if (!userStore.token) {
    next('/login')
    return
  }

  // 权限守卫：路由 meta.permission 不为空时，检查用户是否拥有该权限
  if (to.meta.permission && !userStore.hasPermission(to.meta.permission)) {
    // 无权限 → 跳首页（或 403 页面）
    next('/')
    return
  }

  next()
})

export default router
