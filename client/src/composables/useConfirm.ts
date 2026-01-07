import { ref, readonly } from 'vue'
import type { ConfirmVariant } from '@/components/common/ConfirmModal.vue'

export interface ConfirmOptions {
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  variant?: ConfirmVariant
}

interface ConfirmState {
  isOpen: boolean
  title: string
  message: string
  confirmText: string
  cancelText: string
  variant: ConfirmVariant
  isLoading: boolean
  resolve: ((value: boolean) => void) | null
}

// 전역 상태 (싱글톤)
const state = ref<ConfirmState>({
  isOpen: false,
  title: '',
  message: '',
  confirmText: '확인',
  cancelText: '취소',
  variant: 'danger',
  isLoading: false,
  resolve: null
})

/**
 * 확인 모달 composable
 * confirm() 대신 사용할 수 있는 Promise 기반 확인 다이얼로그
 */
export function useConfirm() {
  /**
   * 확인 모달을 표시하고 사용자 응답을 Promise로 반환합니다.
   * @param options 모달 옵션
   * @returns 확인 시 true, 취소 시 false
   */
  function confirm(options: ConfirmOptions): Promise<boolean> {
    return new Promise(resolve => {
      state.value = {
        isOpen: true,
        title: options.title,
        message: options.message,
        confirmText: options.confirmText || '확인',
        cancelText: options.cancelText || '취소',
        variant: options.variant || 'danger',
        isLoading: false,
        resolve
      }
    })
  }

  /**
   * 확인 버튼 클릭 핸들러
   */
  function handleConfirm() {
    if (state.value.resolve) {
      state.value.resolve(true)
    }
    closeModal()
  }

  /**
   * 취소 버튼 클릭 핸들러
   */
  function handleCancel() {
    if (state.value.resolve) {
      state.value.resolve(false)
    }
    closeModal()
  }

  /**
   * 로딩 상태 설정
   */
  function setLoading(loading: boolean) {
    state.value.isLoading = loading
  }

  /**
   * 모달 닫기
   */
  function closeModal() {
    state.value.isOpen = false
    state.value.resolve = null
    state.value.isLoading = false
  }

  return {
    // 상태 (읽기 전용)
    isOpen: readonly(ref(() => state.value.isOpen)),
    title: readonly(ref(() => state.value.title)),
    message: readonly(ref(() => state.value.message)),
    confirmText: readonly(ref(() => state.value.confirmText)),
    cancelText: readonly(ref(() => state.value.cancelText)),
    variant: readonly(ref(() => state.value.variant)),
    isLoading: readonly(ref(() => state.value.isLoading)),

    // 내부 상태 (ConfirmProvider용)
    state,

    // 메서드
    confirm,
    handleConfirm,
    handleCancel,
    setLoading,
    closeModal
  }
}
