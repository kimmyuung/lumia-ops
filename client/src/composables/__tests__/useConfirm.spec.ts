import { describe, it, expect, beforeEach } from 'vitest'
import { useConfirm } from '../useConfirm'

describe('useConfirm', () => {
  let confirm: ReturnType<typeof useConfirm>

  beforeEach(() => {
    confirm = useConfirm()
    // 이전 테스트에서 남은 상태 초기화
    confirm.closeModal()
  })

  describe('initial state', () => {
    it('should be closed initially', () => {
      expect(confirm.state.value.isOpen).toBe(false)
    })

    it('should have default values', () => {
      expect(confirm.state.value.confirmText).toBe('확인')
      expect(confirm.state.value.cancelText).toBe('취소')
      expect(confirm.state.value.variant).toBe('danger')
      expect(confirm.state.value.isLoading).toBe(false)
    })
  })

  describe('confirm()', () => {
    it('should open modal with provided options', async () => {
      const promise = confirm.confirm({
        title: '테스트 제목',
        message: '테스트 메시지',
        confirmText: '삭제',
        cancelText: '돌아가기',
        variant: 'warning'
      })

      expect(confirm.state.value.isOpen).toBe(true)
      expect(confirm.state.value.title).toBe('테스트 제목')
      expect(confirm.state.value.message).toBe('테스트 메시지')
      expect(confirm.state.value.confirmText).toBe('삭제')
      expect(confirm.state.value.cancelText).toBe('돌아가기')
      expect(confirm.state.value.variant).toBe('warning')

      // Promise가 pending 상태인지 확인 후 취소
      confirm.handleCancel()
      const result = await promise
      expect(result).toBe(false)
    })

    it('should use default values when options are not provided', async () => {
      const promise = confirm.confirm({
        title: '제목',
        message: '메시지'
      })

      expect(confirm.state.value.confirmText).toBe('확인')
      expect(confirm.state.value.cancelText).toBe('취소')
      expect(confirm.state.value.variant).toBe('danger')

      confirm.handleCancel()
      await promise
    })
  })

  describe('handleConfirm()', () => {
    it('should resolve promise with true', async () => {
      const promise = confirm.confirm({
        title: '확인',
        message: '진행하시겠습니까?'
      })

      confirm.handleConfirm()

      const result = await promise
      expect(result).toBe(true)
    })

    it('should close modal after confirm', async () => {
      const promise = confirm.confirm({
        title: '확인',
        message: '진행하시겠습니까?'
      })

      confirm.handleConfirm()
      await promise

      expect(confirm.state.value.isOpen).toBe(false)
    })
  })

  describe('handleCancel()', () => {
    it('should resolve promise with false', async () => {
      const promise = confirm.confirm({
        title: '확인',
        message: '진행하시겠습니까?'
      })

      confirm.handleCancel()

      const result = await promise
      expect(result).toBe(false)
    })

    it('should close modal after cancel', async () => {
      const promise = confirm.confirm({
        title: '확인',
        message: '진행하시겠습니까?'
      })

      confirm.handleCancel()
      await promise

      expect(confirm.state.value.isOpen).toBe(false)
    })
  })

  describe('setLoading()', () => {
    it('should set loading state to true', () => {
      confirm.setLoading(true)
      expect(confirm.state.value.isLoading).toBe(true)
    })

    it('should set loading state to false', () => {
      confirm.setLoading(true)
      confirm.setLoading(false)
      expect(confirm.state.value.isLoading).toBe(false)
    })
  })

  describe('closeModal()', () => {
    it('should close modal and reset state', async () => {
      const promise = confirm.confirm({
        title: '확인',
        message: '메시지'
      })

      confirm.closeModal()

      expect(confirm.state.value.isOpen).toBe(false)
      expect(confirm.state.value.resolve).toBeNull()
      expect(confirm.state.value.isLoading).toBe(false)

      // Promise는 resolve되지 않은 상태로 남음
      // 실제 사용에서는 handleConfirm이나 handleCancel 사용
      void promise
    })
  })
})
