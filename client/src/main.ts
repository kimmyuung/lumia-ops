import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import pinia from './stores'
import { useUserStore } from './stores/user'
import { userApi } from './api/user'
import { logErrorToServer } from './utils/errorLogger'

const app = createApp(App)

// 전역 Vue 에러 핸들러
app.config.errorHandler = (err, instance, info) => {
  console.error('[Vue Error]', err)
  console.error('[Component]', instance)
  console.error('[Info]', info)

  // 서버로 에러 전송
  logErrorToServer(err as Error, {
    componentName: instance?.$options?.name,
    propsData: instance?.$props,
    info
  })
}

// 전역 경고 핸들러 (개발 모드)
app.config.warnHandler = (msg, _instance, trace) => {
  console.warn('[Vue Warning]', msg)
  if (trace) console.warn('[Trace]', trace)
}

// 처리되지 않은 Promise 거부 핸들러
window.addEventListener('unhandledrejection', event => {
  console.error('[Unhandled Promise Rejection]', event.reason)

  // 서버로 에러 전송
  const error = event.reason instanceof Error
    ? event.reason
    : new Error(String(event.reason))
  logErrorToServer(error, { type: 'unhandledRejection' })

  event.preventDefault()
})

// 전역 JavaScript 에러 핸들러
window.addEventListener('error', event => {
  console.error('[Global Error]', event.error || event.message)

  // 서버로 에러 전송
  const error = event.error || new Error(event.message)
  logErrorToServer(error, {
    type: 'globalError',
    filename: event.filename,
    lineno: event.lineno,
    colno: event.colno
  })
})

// 토큰 만료 이벤트 리스너
window.addEventListener('auth:token-expired', () => {
  console.info('[Auth] 토큰 만료 이벤트 수신, 로그인 페이지로 이동합니다.')
  router.push({ name: 'Login', query: { expired: '1' } })
})

app.use(pinia)
app.use(router)

// 앱 초기화 시 저장된 토큰 검증 및 사용자 정보 로드
async function initializeAuth() {
  const userStore = useUserStore()
  const hasValidToken = userStore.loadToken()

  if (hasValidToken && userStore.token) {
    try {
      const userData = await userApi.getMyInfo()
      userStore.setUser({
        id: String(userData.id),
        email: userData.email,
        nickname: userData.nickname || '',
        status: userData.status
      })
      console.info('[Auth] 사용자 정보 로드 완료')
    } catch (error) {
      console.warn('[Auth] 사용자 정보 로드 실패, 로그아웃 처리')
      userStore.logout()
    }
  }
}

// 앱 마운트 전 인증 초기화
initializeAuth().finally(() => {
  app.mount('#app')
})
