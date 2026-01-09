<template>
  <div class="callback-page">
    <div class="callback-container">
      <div v-if="isLoading" class="loading-state">
        <Spinner />
        <p>{{ providerName }} 로그인 처리 중...</p>
      </div>

      <div v-else-if="error" class="error-state">
        <AlertCircle :size="48" />
        <h2>로그인 실패</h2>
        <p>{{ error }}</p>
        <Button variant="primary" @click="goToLogin">
          <LogIn :size="18" />
          <span>로그인 페이지로 이동</span>
        </Button>
      </div>

      <div v-else-if="needsGameNickname" class="nickname-setup">
        <h2>이터널 리턴 닉네임 설정</h2>
        <p>플랫폼 기능을 사용하려면 이터널 리턴 인게임 닉네임을 입력해주세요.</p>

        <div class="form-field">
          <Input
            v-model="gameNickname"
            placeholder="이터널 리턴 닉네임"
            :error="nicknameError"
            :disabled="isSettingNickname"
          />
        </div>

        <Button
          variant="primary"
          :loading="isSettingNickname"
          :disabled="!gameNickname.trim()"
          @click="handleSetupNickname"
        >
          설정 완료
        </Button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { AlertCircle, LogIn } from 'lucide-vue-next'
import { Button, Input } from '@/components/common'
import Spinner from '@/components/common/Spinner.vue'
import { oauth2Api } from '@/api/auth'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const toast = useToast()

const isLoading = ref(true)
const error = ref('')
const needsGameNickname = ref(false)
const gameNickname = ref('')
const nicknameError = ref('')
const isSettingNickname = ref(false)

const provider = computed(() => route.params.provider as string)
const providerName = computed(() => (provider.value === 'steam' ? 'Steam' : 'Kakao'))

onMounted(async () => {
  try {
    if (provider.value === 'steam') {
      await handleSteamCallback()
    } else if (provider.value === 'kakao') {
      await handleKakaoCallback()
    } else {
      error.value = '지원하지 않는 로그인 방식입니다.'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '로그인 처리 중 오류가 발생했습니다.'
  } finally {
    isLoading.value = false
  }
})

async function handleSteamCallback() {
  // Steam OpenID 응답에서 Steam ID 추출
  const claimedId = route.query['openid.claimed_id'] as string
  if (!claimedId) {
    throw new Error('Steam 인증 정보를 찾을 수 없습니다.')
  }

  // Steam ID 추출 (URL 형식: https://steamcommunity.com/openid/id/76561198xxxxxxxxx)
  const steamIdMatch = claimedId.match(/\/id\/(\d+)$/)
  if (!steamIdMatch) {
    throw new Error('Steam ID를 추출할 수 없습니다.')
  }

  const steamId = steamIdMatch[1] ?? ''
  if (!steamId) {
    throw new Error('Steam ID를 추출할 수 없습니다.')
  }

  // Phase 2에서 Steam Web API 통합 예정 (GetPlayerSummaries)
  // 현재는 임시 닉네임 사용, 사용자가 설정 화면에서 변경 가능
  const response = await oauth2Api.steamCallback({
    steamId: steamId,
    steamNickname: `User${steamId.slice(-6)}`
  })

  handleLoginSuccess(response)
}

async function handleKakaoCallback() {
  const code = route.query.code as string
  if (!code) {
    throw new Error('Kakao 인증 코드를 찾을 수 없습니다.')
  }

  // 백엔드에서 토큰 교환 및 로그인 처리
  const response = await oauth2Api.kakaoCodeCallback(code)
  handleLoginSuccess(response)
}

function handleLoginSuccess(response: Awaited<ReturnType<typeof oauth2Api.steamCallback>>) {
  // 토큰 저장
  localStorage.setItem('token', response.token)
  localStorage.setItem('refreshToken', response.refreshToken)

  if (response.needsGameNickname) {
    needsGameNickname.value = true
  } else {
    toast.success('로그인 성공!')
    router.push('/')
  }
}

async function handleSetupNickname() {
  if (!gameNickname.value.trim()) {
    nicknameError.value = '닉네임을 입력해주세요.'
    return
  }

  isSettingNickname.value = true
  nicknameError.value = ''

  try {
    await oauth2Api.setupGameNickname(gameNickname.value.trim())
    toast.success('닉네임 설정이 완료되었습니다!')
    router.push('/')
  } catch (e) {
    nicknameError.value =
      e instanceof Error ? e.message : '닉네임 설정에 실패했습니다. 유효한 닉네임인지 확인해주세요.'
  } finally {
    isSettingNickname.value = false
  }
}

function goToLogin() {
  router.push('/login')
}
</script>

<style scoped>
.callback-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.callback-container {
  width: 100%;
  max-width: 420px;
  background: var(--card-bg);
  backdrop-filter: var(--glass-blur);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: 2.5rem;
  box-shadow: var(--shadow-lg);
  text-align: center;
}

.loading-state,
.error-state,
.nickname-setup {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.loading-state p,
.error-state p {
  color: var(--text-muted);
}

.error-state h2 {
  color: var(--danger-color);
}

.nickname-setup h2 {
  color: var(--text-color);
}

.nickname-setup p {
  color: var(--text-muted);
  margin-bottom: 1rem;
}

.form-field {
  width: 100%;
  margin-bottom: 1rem;
}
</style>
