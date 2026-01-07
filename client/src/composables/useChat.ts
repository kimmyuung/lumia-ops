import { ref, computed, type Ref } from 'vue'
import { useStompClient } from './useStompClient'

/**
 * 채팅 메시지 타입
 */
export const MessageType = {
  CHAT: 'CHAT',
  JOIN: 'JOIN',
  LEAVE: 'LEAVE',
  SYSTEM: 'SYSTEM'
} as const

export type MessageTypeValue = (typeof MessageType)[keyof typeof MessageType]

/**
 * 채팅 메시지
 */
export interface ChatMessage {
  type: MessageTypeValue
  roomId: string
  sender: string
  senderId?: number
  content: string
  timestamp?: string
}

/**
 * useChat 옵션
 */
export interface UseChatOptions {
  /** WebSocket 서버 URL */
  url?: string
  /** JWT 토큰 */
  token?: string | Ref<string | null>
  /** 자동 연결 */
  autoConnect?: boolean
}

/**
 * 채팅 기능 composable
 *
 * STOMP 클라이언트를 사용하여 실시간 채팅 기능을 제공합니다.
 *
 * @example
 * ```typescript
 * const { connect, joinRoom, sendMessage, messages, isConnected } = useChat()
 *
 * await connect()
 * await joinRoom('room-123', 'username')
 * sendMessage('room-123', 'Hello!')
 * ```
 */
export function useChat(options: UseChatOptions = {}) {
  const { url, token, autoConnect = false } = options

  const messages = ref<ChatMessage[]>([])
  const currentRoomId = ref<string | null>(null)
  const currentSender = ref<string | null>(null)

  const {
    isConnected,
    error,
    connect: stompConnect,
    disconnect: stompDisconnect,
    subscribe,
    unsubscribe,
    send
  } = useStompClient({
    url,
    token,
    autoConnect
  })

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
    if (currentRoomId.value) {
      leaveRoom()
    }
    stompDisconnect()
  }

  /**
   * 채팅방 입장
   */
  const joinRoom = (roomId: string, sender: string, senderId?: number) => {
    if (!isConnected.value) {
      console.warn('[Chat] Cannot join room: not connected')
      return false
    }

    // 이전 방 퇴장
    if (currentRoomId.value && currentRoomId.value !== roomId) {
      leaveRoom()
    }

    currentRoomId.value = roomId
    currentSender.value = sender
    messages.value = []

    // 채팅방 구독
    subscribe<ChatMessage>(`/topic/room/${roomId}`, message => {
      messages.value.push(message.body)
    })

    // 입장 메시지 전송
    send<ChatMessage>(`/app/chat.join/${roomId}`, {
      type: MessageType.JOIN,
      roomId,
      sender,
      senderId,
      content: ''
    })

    return true
  }

  /**
   * 채팅방 퇴장
   */
  const leaveRoom = () => {
    if (!currentRoomId.value || !currentSender.value) return

    // 퇴장 메시지 전송
    send<ChatMessage>(`/app/chat.leave/${currentRoomId.value}`, {
      type: MessageType.LEAVE,
      roomId: currentRoomId.value,
      sender: currentSender.value,
      content: ''
    })

    // 구독 해제
    unsubscribe(`/topic/room/${currentRoomId.value}`)
    currentRoomId.value = null
    currentSender.value = null
    messages.value = []
  }

  /**
   * 메시지 전송
   */
  const sendMessage = (content: string, senderId?: number): boolean => {
    if (!currentRoomId.value || !currentSender.value) {
      console.warn('[Chat] Cannot send message: not in a room')
      return false
    }

    return send<ChatMessage>(`/app/chat.send/${currentRoomId.value}`, {
      type: MessageType.CHAT,
      roomId: currentRoomId.value,
      sender: currentSender.value,
      senderId,
      content
    })
  }

  /**
   * 메시지 목록 초기화
   */
  const clearMessages = () => {
    messages.value = []
  }

  return {
    // State
    isConnected,
    error,
    messages,
    currentRoomId: computed(() => currentRoomId.value),
    currentSender: computed(() => currentSender.value),

    // Actions
    connect,
    disconnect,
    joinRoom,
    leaveRoom,
    sendMessage,
    clearMessages
  }
}

export type UseChatReturn = ReturnType<typeof useChat>
