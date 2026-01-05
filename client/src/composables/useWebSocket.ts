import { ref, onUnmounted } from 'vue'
import { useChat, MessageType } from './useChat'

/**
 * @deprecated useChat 또는 useStompClient를 직접 사용하세요.
 * 이 composable은 하위 호환성을 위해 유지됩니다.
 * 
 * 새로운 채팅 기능:
 * ```typescript
 * import { useChat } from '@/composables/useChat'
 * const { connect, joinRoom, sendMessage, messages } = useChat()
 * ```
 * 
 * 커스텀 STOMP 기능:
 * ```typescript
 * import { useStompClient } from '@/composables/useStompClient'
 * const { connect, subscribe, send } = useStompClient()
 * ```
 */

// Re-export for backward compatibility
export { MessageType }

interface LegacyChatMessage {
    type: 'CHAT' | 'JOIN' | 'LEAVE'
    roomId: string
    sender: string
    senderId?: number
    content: string
    timestamp: string
}

interface UseWebSocketOptions {
    url?: string
    onMessage?: (message: LegacyChatMessage) => void
    onConnect?: () => void
    onDisconnect?: () => void
    onError?: (error: Event) => void
}

/**
 * @deprecated useChat을 직접 사용하세요.
 */
export function useWebSocket(options: UseWebSocketOptions = {}) {
    const {
        url = import.meta.env.VITE_WS_URL || 'http://localhost:8081/ws',
        onMessage: _onMessage,
        onConnect,
        onDisconnect,
        onError: _onError
    } = options

    // 내부적으로 useChat 사용
    const chat = useChat({ url })

    const messages = ref<LegacyChatMessage[]>([])
    const error = ref<string | null>(null)

    // useChat의 상태 매핑
    const isConnected = chat.isConnected

    const connect = () => {
        chat.connect()
        // 연결 상태 감시
        const checkConnection = setInterval(() => {
            if (chat.isConnected.value) {
                clearInterval(checkConnection)
                onConnect?.()
            }
        }, 100)

        // 5초 후 타임아웃
        setTimeout(() => clearInterval(checkConnection), 5000)
    }

    const disconnect = () => {
        chat.disconnect()
        onDisconnect?.()
    }



    const joinRoom = (roomId: string, sender: string): boolean => {
        return chat.joinRoom(roomId, sender)
    }

    const leaveRoom = (_roomId: string, _sender: string): boolean => {
        chat.leaveRoom()
        return true
    }

    const sendMessage = (roomId: string, sender: string, content: string): boolean => {
        // 방에 입장하지 않은 경우 먼저 입장
        if (chat.currentRoomId.value !== roomId) {
            chat.joinRoom(roomId, sender)
        }
        return chat.sendMessage(content)
    }

    const clearMessages = () => {
        messages.value = []
        chat.clearMessages()
    }

    onUnmounted(() => {
        disconnect()
    })

    return {
        // State
        isConnected,
        messages,
        error,

        // Actions
        connect,
        disconnect,
        joinRoom,
        leaveRoom,
        sendMessage,
        clearMessages
    }
}

export type { LegacyChatMessage, UseWebSocketOptions }

