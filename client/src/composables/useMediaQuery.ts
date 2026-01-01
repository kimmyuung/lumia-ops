import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 반응형 미디어 쿼리 훅
 */
export function useMediaQuery(query: string) {
    const matches = ref(false)

    let mediaQuery: MediaQueryList | null = null

    const handler = (e: MediaQueryListEvent) => {
        matches.value = e.matches
    }

    onMounted(() => {
        mediaQuery = window.matchMedia(query)
        matches.value = mediaQuery.matches
        mediaQuery.addEventListener('change', handler)
    })

    onUnmounted(() => {
        mediaQuery?.removeEventListener('change', handler)
    })

    return matches
}

/**
 * 모바일 여부 확인 훅
 */
export function useIsMobile() {
    return useMediaQuery('(max-width: 768px)')
}
