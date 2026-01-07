import { ref, type Ref } from 'vue'
import { useStompClient } from './useStompClient'

/**
 * 전략 변경 타입
 */
export type StrategyChangeType = 'ADD' | 'UPDATE' | 'DELETE' | 'MOVE'

/**
 * 전략 업데이트 메시지
 */
export interface StrategyUpdateMessage {
  type: StrategyChangeType
  sender: string
  senderId: number
  data: Record<string, unknown>
  timestamp?: string
}

/**
 * 커서 위치
 */
export interface CursorPosition {
  userId: number
  username: string
  x: number
  y: number
}

/**
 * 세션 참여 메시지
 */
export interface StrategyJoinMessage {
  userId: number
  username: string
  message?: string
}

/**
 * useStrategySync 옵션
 */
export interface UseStrategySyncOptions {
  /** WebSocket 서버 URL */
  url?: string
  /** JWT 토큰 */
  token?: string | Ref<string | null>
  /** 전략 변경 콜백 */
  onUpdate?: (message: StrategyUpdateMessage) => void
  /** 커서 위치 변경 콜백 */
  onCursorMove?: (cursor: CursorPosition) => void
  /** 참가자 변경 콜백 */
  onParticipantJoin?: (message: StrategyJoinMessage) => void
}

/**
 * 전략 맵 실시간 동기화 composable
 *
 * 전략 맵의 변경사항, 커서 위치, 참가자 정보를 실시간으로 동기화합니다.
 *
 * @example
 * ```typescript
 * const { connect, joinStrategy, sendUpdate, shareCursor, isConnected } = useStrategySync({
 *   onUpdate: (msg) => applyChange(msg),
 *   onCursorMove: (cursor) => showCursor(cursor)
 * })
 *
 * await connect()
 * joinStrategy('strategy-123', 1, 'username')
 *
 * // 변경사항 전송
 * sendUpdate({ type: 'UPDATE', data: { elementId: 'xxx', ... } })
 *
 * // 커서 위치 공유
 * shareCursor(100, 200)
 * ```
 */
export function useStrategySync(options: UseStrategySyncOptions = {}) {
  const { url, token, onUpdate, onCursorMove, onParticipantJoin: _onParticipantJoin } = options

  const currentStrategyId = ref<string | null>(null)
  const currentUserId = ref<number | null>(null)
  const currentUsername = ref<string | null>(null)
  const participants = ref<StrategyJoinMessage[]>([])
  const cursors = ref<Map<number, CursorPosition>>(new Map())
  const updates = ref<StrategyUpdateMessage[]>([])

  const {
    isConnected,
    error,
    connect: stompConnect,
    disconnect: stompDisconnect,
    subscribe,
    unsubscribe,
    unsubscribeAll: _unsubscribeAll,
    send
  } = useStompClient({ url, token })

  /**
   * STOMP 서버 연결
   */
  const connect = () => {
    stompConnect()
  }

  /**
   * 연결 해제
   */
  const disconnect = () => {
    leaveStrategy()
    stompDisconnect()
  }

  /**
   * 전략 편집 세션 참여
   */
  const joinStrategy = (strategyId: string, userId: number, username: string) => {
    if (!isConnected.value) {
      console.warn('[StrategySync] Cannot join: not connected')
      return false
    }

    // 이전 전략 세션 퇴장
    if (currentStrategyId.value) {
      leaveStrategy()
    }

    currentStrategyId.value = strategyId
    currentUserId.value = userId
    currentUsername.value = username
    participants.value = []
    cursors.value.clear()
    updates.value = []

    // 전략 업데이트 구독
    subscribe<StrategyUpdateMessage>(`/topic/strategy/${strategyId}`, message => {
      updates.value.push(message.body)
      onUpdate?.(message.body)
    })

    // 커서 위치 구독
    subscribe<CursorPosition>(`/topic/strategy/${strategyId}/cursors`, message => {
      cursors.value.set(message.body.userId, message.body)
      onCursorMove?.(message.body)
    })

    // 참여 메시지 전송
    send<StrategyJoinMessage>(`/app/strategy.join/${strategyId}`, {
      userId,
      username
    })

    return true
  }

  /**
   * 전략 편집 세션 퇴장
   */
  const leaveStrategy = () => {
    if (!currentStrategyId.value) return

    unsubscribe(`/topic/strategy/${currentStrategyId.value}`)
    unsubscribe(`/topic/strategy/${currentStrategyId.value}/cursors`)

    currentStrategyId.value = null
    currentUserId.value = null
    currentUsername.value = null
    participants.value = []
    cursors.value.clear()
  }

  /**
   * 전략 변경사항 전송
   */
  const sendUpdate = (type: StrategyChangeType, data: Record<string, unknown>): boolean => {
    if (!currentStrategyId.value || !currentUserId.value || !currentUsername.value) {
      console.warn('[StrategySync] Cannot send update: not in a session')
      return false
    }

    return send<StrategyUpdateMessage>(`/app/strategy.update/${currentStrategyId.value}`, {
      type,
      sender: currentUsername.value,
      senderId: currentUserId.value,
      data
    })
  }

  /**
   * 커서 위치 공유
   */
  const shareCursor = (x: number, y: number): boolean => {
    if (!currentStrategyId.value || !currentUserId.value || !currentUsername.value) {
      return false
    }

    return send<CursorPosition>(`/app/strategy.cursor/${currentStrategyId.value}`, {
      userId: currentUserId.value,
      username: currentUsername.value,
      x,
      y
    })
  }

  return {
    // State
    isConnected,
    error,
    currentStrategyId,
    participants,
    cursors,
    updates,

    // Actions
    connect,
    disconnect,
    joinStrategy,
    leaveStrategy,
    sendUpdate,
    shareCursor
  }
}

export type UseStrategySyncReturn = ReturnType<typeof useStrategySync>
