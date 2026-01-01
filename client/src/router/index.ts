import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue')
  },
  {
    path: '/team',
    name: 'Team',
    component: () => import('@/views/TeamView.vue')
  },
  {
    path: '/strategy',
    name: 'Strategy',
    component: () => import('@/views/StrategyView.vue')
  },
  {
    path: '/scrim',
    name: 'Scrim',
    component: () => import('@/views/ScrimView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router
