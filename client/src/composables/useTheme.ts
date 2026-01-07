import { ref, watch, onMounted } from 'vue'

export type Theme = 'light' | 'dark' | 'system'

const THEME_STORAGE_KEY = 'lumia-theme'

// 전역 상태 (싱글톤)
const currentTheme = ref<Theme>('system')
const resolvedTheme = ref<'light' | 'dark'>('light')

/**
 * 테마 관리 composable
 * 라이트/다크/시스템 모드를 지원합니다.
 */
export function useTheme() {
  /**
   * 시스템 테마 감지
   */
  function getSystemTheme(): 'light' | 'dark' {
    if (typeof window === 'undefined') return 'light'
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  }

  /**
   * 테마를 DOM에 적용
   */
  function applyTheme(theme: Theme) {
    const resolved = theme === 'system' ? getSystemTheme() : theme
    resolvedTheme.value = resolved

    // HTML 요소에 data-theme 속성 설정
    document.documentElement.setAttribute('data-theme', resolved)

    // meta theme-color 업데이트 (모바일 브라우저용)
    const metaThemeColor = document.querySelector('meta[name="theme-color"]')
    if (metaThemeColor) {
      metaThemeColor.setAttribute('content', resolved === 'dark' ? '#0f0f1a' : '#f5f7fa')
    }
  }

  /**
   * 테마 변경
   */
  function setTheme(theme: Theme) {
    currentTheme.value = theme
    localStorage.setItem(THEME_STORAGE_KEY, theme)
    applyTheme(theme)
  }

  /**
   * 테마 토글 (light ↔ dark, system이면 현재 resolved 기준)
   */
  function toggleTheme() {
    if (currentTheme.value === 'system') {
      // 시스템 모드일 때는 현재 적용된 테마의 반대로 설정
      setTheme(resolvedTheme.value === 'dark' ? 'light' : 'dark')
    } else {
      setTheme(currentTheme.value === 'dark' ? 'light' : 'dark')
    }
  }

  /**
   * 저장된 테마 불러오기
   */
  function loadTheme() {
    const saved = localStorage.getItem(THEME_STORAGE_KEY) as Theme | null
    if (saved && ['light', 'dark', 'system'].includes(saved)) {
      currentTheme.value = saved
    }
    applyTheme(currentTheme.value)
  }

  /**
   * 시스템 테마 변경 감지
   */
  function setupSystemThemeListener() {
    if (typeof window === 'undefined') return

    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')

    const handler = () => {
      if (currentTheme.value === 'system') {
        applyTheme('system')
      }
    }

    // 이벤트 리스너 등록
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', handler)
    } else {
      // Safari 14 이하 호환
      mediaQuery.addListener(handler)
    }
  }

  // 컴포넌트 마운트 시 초기화
  onMounted(() => {
    loadTheme()
    setupSystemThemeListener()
  })

  // 테마 변경 감시
  watch(currentTheme, newTheme => {
    applyTheme(newTheme)
  })

  return {
    currentTheme,
    resolvedTheme,
    setTheme,
    toggleTheme,
    loadTheme
  }
}
