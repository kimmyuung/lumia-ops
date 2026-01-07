import { ref, onUnmounted, type Ref } from 'vue'
import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

/**
 * STOMP 메시지 타입
 */
export interface StompMessage<T = unknown> {
  body: T
  headers: Record<string, string>
  destination: string
}

/**
 * useStompClient 옵션
 */
export interface UseStompClientOptions {
  /** WebSocket 서버 URL (SockJS 엔드포인트) */
  url?: string
  /** JWT 토큰 (인증 헤더용) */
  token?: string | Ref<string | null>
  /** 연결 성공 콜백 */
  onConnect?: () => void
  /** 연결 해제 콜백 */
  onDisconnect?: () => void
  /** 에러 콜백 */
  onError?: (error: string) => void
  /** 자동 연결 여부 */
  autoConnect?: boolean
  /** 재연결 딜레이 (ms) */
  reconnectDelay?: number
  /** 하트비트 (ms) */
  heartbeatIncoming?: number
  heartbeatOutgoing?: number
}

/**
 * STOMP 클라이언트 composable
 *
 * SockJS + STOMP 프로토콜을 사용하여 백엔드 WebSocket 서버와 통신합니다.
 *
 * @example
 * ```typescript
 * const { connect, subscribe, send, isConnected } = useStompClient({
 *   url: 'http://localhost:8081/ws',
 *   token: authStore.token
 * })
 *
 * connect()
 *
 * subscribe('/topic/room/123', (message) => {
 *   console.log('Received:', message.body)
 * })
 *
 * send('/app/chat.send/123', { content: 'Hello' })
 * ```
 */
export function useStompClient(options: UseStompClientOptions = {}) {
  const {
    url = import.meta.env.VITE_WS_URL || 'http://localhost:8081/ws',
    token,
    onConnect,
    onDisconnect,
    onError,
    autoConnect = false,
    reconnectDelay = 1000, // 초기 재연결 딜레이 (1초)
    heartbeatIncoming = 10000,
    heartbeatOutgoing = 10000
  } = options

  const isConnected = ref(false)
  const error = ref<string | null>(null)
  const subscriptions = ref<Map<string, StompSubscription>>(new Map())
  const reconnectAttempts = ref(0)

  // 지수 백오프 설정
  const MAX_RECONNECT_DELAY = 30000 // 최대 30초
  const MAX_RECONNECT_ATTEMPTS = 10 // 최대 10회 시도

  let client: Client | null = null

  /**
   * 현재 토큰 값 가져오기
   */
  const getToken = (): string | null => {
    if (typeof token === 'string') return token
    if (token && 'value' in token) return token.value
    // localStorage에서 폴백
    return localStorage.getItem('token')
  }

  /**
   * 지수 백오프 딜레이 계산
   * @param attempt 현재 재연결 시도 횟수
   * @returns 딜레이 (ms)
   */
  const calculateBackoffDelay = (attempt: number): number => {
    // 지수 백오프: delay * 2^attempt (최대 MAX_RECONNECT_DELAY)
    const delay = Math.min(reconnectDelay * Math.pow(2, attempt), MAX_RECONNECT_DELAY)
    // 약간의 지터(jitter) 추가 (±10%)
    const jitter = delay * 0.1 * (Math.random() * 2 - 1)
    return Math.floor(delay + jitter)
  }

  /**
   * STOMP 클라이언트 연결
   */
  const connect = () => {
    if (client?.connected) return

    client = new Client({
      webSocketFactory: () => new SockJS(url),
      connectHeaders: getToken() ? { Authorization: `Bearer ${getToken()}` } : {},

      // 지수 백오프로 재연결 딜레이 계산
      reconnectDelay: calculateBackoffDelay(reconnectAttempts.value),

      heartbeatIncoming,
      heartbeatOutgoing,

      onConnect: () => {
        isConnected.value = true
        error.value = null
        reconnectAttempts.value = 0 // 성공 시 재연결 시도 횟수 초기화
        console.log('[STOMP] Connected')
        onConnect?.()
      },

      onDisconnect: () => {
        isConnected.value = false
        console.log('[STOMP] Disconnected')
        onDisconnect?.()
      },

      onStompError: frame => {
        const errorMessage = frame.headers['message'] || 'STOMP error'
        error.value = errorMessage
        console.error('[STOMP] Error:', errorMessage)
        onError?.(errorMessage)
      },

      onWebSocketError: _event => {
        error.value = 'WebSocket connection error'
        reconnectAttempts.value++

        if (reconnectAttempts.value >= MAX_RECONNECT_ATTEMPTS) {
          console.error('[STOMP] Max reconnection attempts reached')
          client?.deactivate()
        } else {
          const nextDelay = calculateBackoffDelay(reconnectAttempts.value)
          console.log(
            `[STOMP] WebSocket error. Reconnecting in ${nextDelay}ms (attempt ${reconnectAttempts.value}/${MAX_RECONNECT_ATTEMPTS})`
          )
        }

        onError?.('WebSocket connection error')
      },

      // 재연결 시도 전 콜백
      beforeConnect: () => {
        if (reconnectAttempts.value > 0) {
          console.log(
            `[STOMP] Reconnection attempt ${reconnectAttempts.value}/${MAX_RECONNECT_ATTEMPTS}`
          )
        }
      }
    })

    client.activate()
  }

  /**
   * STOMP 클라이언트 연결 해제
   */
  const disconnect = () => {
    if (client) {
      // 모든 구독 해제
      subscriptions.value.forEach(sub => sub.unsubscribe())
      subscriptions.value.clear()

      client.deactivate()
      client = null
      isConnected.value = false
    }
  }

  /**
   * 토픽 구독
   *
   * @param destination 구독할 destination (예: /topic/room/123)
   * @param callback 메시지 수신 콜백
   * @returns 구독 해제 함수
   */
  const subscribe = <T = unknown>(
    destination: string,
    callback: (message: StompMessage<T>) => void
  ): (() => void) => {
    if (!client?.connected) {
      console.warn('[STOMP] Cannot subscribe: not connected')
      return () => {}
    }

    // 이미 구독 중이면 기존 구독 해제
    if (subscriptions.value.has(destination)) {
      subscriptions.value.get(destination)?.unsubscribe()
    }

    const subscription = client.subscribe(destination, (message: IMessage) => {
      try {
        const body = JSON.parse(message.body) as T
        callback({
          body,
          headers: message.headers as Record<string, string>,
          destination
        })
      } catch (e) {
        console.error('[STOMP] Failed to parse message:', e)
      }
    })

    subscriptions.value.set(destination, subscription)

    return () => {
      subscription.unsubscribe()
      subscriptions.value.delete(destination)
    }
  }

  /**
   * 메시지 전송
   *
   * @param destination 전송할 destination (예: /app/chat.send/123)
   * @param body 메시지 본문
   * @param headers 추가 헤더
   */
  const send = <T = unknown>(
    destination: string,
    body: T,
    headers: Record<string, string> = {}
  ): boolean => {
    if (!client?.connected) {
      console.warn('[STOMP] Cannot send: not connected')
      return false
    }

    client.publish({
      destination,
      body: JSON.stringify(body),
      headers
    })

    return true
  }

  /**
   * 특정 destination 구독 해제
   */
  const unsubscribe = (destination: string): void => {
    const subscription = subscriptions.value.get(destination)
    if (subscription) {
      subscription.unsubscribe()
      subscriptions.value.delete(destination)
    }
  }

  /**
   * 모든 구독 해제
   */
  const unsubscribeAll = (): void => {
    subscriptions.value.forEach(sub => sub.unsubscribe())
    subscriptions.value.clear()
  }

  // 자동 연결
  if (autoConnect) {
    connect()
  }

  // 컴포넌트 언마운트 시 정리
  onUnmounted(() => {
    disconnect()
  })

  return {
    // State
    isConnected,
    error,
    subscriptions,

    // Actions
    connect,
    disconnect,
    subscribe,
    unsubscribe,
    unsubscribeAll,
    send
  }
}

export type UseStompClientReturn = ReturnType<typeof useStompClient>
