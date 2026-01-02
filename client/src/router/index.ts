import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// 라우트 메타 타입 확장
declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    guestOnly?: boolean
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/team',
    name: 'Team',
    component: () => import('@/views/TeamView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/strategy',
    name: 'Strategy',
    component: () => import('@/views/StrategyView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/scrim',
    name: 'Scrim',
    component: () => import('@/views/ScrimView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/invitations',
    name: 'Invitations',
    component: () => import('@/views/InvitationsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/invite/:token',
    name: 'AcceptInvitation',
    component: () => import('@/views/AcceptInvitationView.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 인증 가드
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const isAuthenticated = !!token

  // 인증이 필요한 페이지
  if (to.meta.requiresAuth && !isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 게스트 전용 페이지 (로그인 상태에서 접근 불가)
  if (to.meta.guestOnly && isAuthenticated) {
    next({ name: 'Home' })
    return
  }

  next()
})

export default router
