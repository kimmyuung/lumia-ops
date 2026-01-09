import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// 라우트 메타 타입 확장
declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    guestOnly?: boolean
    requiresNickname?: boolean // 닉네임 설정 필요
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
  // 인증 관련 라우트
  {
    path: '/auth/verify',
    name: 'EmailVerify',
    component: () => import('@/views/EmailVerifyView.vue')
  },
  {
    path: '/auth/set-nickname',
    name: 'SetNickname',
    component: () => import('@/views/SetNicknameView.vue'),
    meta: { requiresNickname: true }
  },
  {
    path: '/auth/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/ForgotPasswordView.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/auth/reset-password',
    name: 'ResetPassword',
    component: () => import('@/views/ResetPasswordView.vue')
  },
  {
    path: '/auth/find-username',
    name: 'FindUsername',
    component: () => import('@/views/FindUsernameView.vue'),
    meta: { guestOnly: true }
  },
  // OAuth2 콜백 라우트
  {
    path: '/auth/oauth2/:provider/callback',
    name: 'OAuth2Callback',
    component: () => import('@/views/OAuth2CallbackView.vue')
  },
  // 보호된 라우트
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
    path: '/statistics',
    name: 'Statistics',
    component: () => import('@/views/StatisticsView.vue'),
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
