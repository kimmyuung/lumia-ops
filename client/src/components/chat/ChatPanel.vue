<script setup lang="ts">
import { ref, computed, nextTick, watch, onMounted } from 'vue'
import { useChat, type ChatMessage, MessageType } from '@/composables/useChat'

interface Props {
  roomId: string
  username: string
  userId?: number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'close'): void
}>()

const messageInput = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const hasJoinedRoom = ref(false)

const { isConnected, messages, error, connect, disconnect, joinRoom, leaveRoom, sendMessage } =
  useChat({
    url: import.meta.env.VITE_WS_URL || 'http://localhost:8081/ws'
  })

// 채팅방 메시지만 필터링
const roomMessages = computed(() => messages.value.filter(m => m.roomId === props.roomId))

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 메시지 변경 시 스크롤
watch(messages, () => {
  scrollToBottom()
}, { deep: true })

// 연결 후 방 입장
watch(isConnected, (connected) => {
  if (connected && !hasJoinedRoom.value) {
    joinRoom(props.roomId, props.username, props.userId)
    hasJoinedRoom.value = true
  }
})

const handleSend = () => {
  const content = messageInput.value.trim()
  if (!content) return

  sendMessage(content, props.userId)
  messageInput.value = ''
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

const handleClose = () => {
  leaveRoom()
  disconnect()
  emit('close')
}

const formatTime = (timestamp?: string) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
}

const getMessageClass = (message: ChatMessage) => {
  if (message.type === MessageType.JOIN || message.type === MessageType.LEAVE) {
    return 'system-message'
  }
  return message.sender === props.username ? 'my-message' : 'other-message'
}

// 컴포넌트 마운트 시 연결
onMounted(() => {
  connect()
})

// 언마운트 시 정리는 composable에서 처리됨
</script>

<template>
  <div class="chat-panel">
    <!-- Header -->
    <div class="chat-header">
      <div class="header-info">
        <h3>채팅</h3>
        <span class="room-id">{{ roomId }}</span>
      </div>
      <div class="header-actions">
        <span :class="['status-dot', { connected: isConnected }]"></span>
        <button class="close-btn" @click="handleClose">✕</button>
      </div>
    </div>

    <!-- Error -->
    <div v-if="error" class="error-banner">
      {{ error }}
    </div>

    <!-- Messages -->
    <div ref="messagesContainer" class="messages-container">
      <div
        v-for="(message, index) in roomMessages"
        :key="index"
        :class="['message', getMessageClass(message)]"
      >
        <template v-if="message.type === 'JOIN' || message.type === 'LEAVE'">
          <span class="system-text">{{ message.content }}</span>
        </template>
        <template v-else>
          <div class="message-header">
            <span class="sender">{{ message.sender }}</span>
            <span class="time">{{ formatTime(message.timestamp) }}</span>
          </div>
          <div class="message-content">{{ message.content }}</div>
        </template>
      </div>

      <div v-if="roomMessages.length === 0" class="empty-state">
        아직 메시지가 없습니다. 첫 메시지를 보내보세요!
      </div>
    </div>

    <!-- Input -->
    <div class="input-container">
      <textarea
        v-model="messageInput"
        placeholder="메시지를 입력하세요..."
        :disabled="!isConnected"
        @keydown="handleKeydown"
      ></textarea>
      <button class="send-btn" :disabled="!isConnected || !messageInput.trim()" @click="handleSend">
        전송
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: var(--color-background-soft);
  border-bottom: 1px solid var(--color-border);
}

.header-info h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.room-id {
  font-size: 0.75rem;
  color: var(--color-text-mute);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
}

.status-dot.connected {
  background: #22c55e;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: var(--color-text-mute);
  padding: 4px 8px;
}

.close-btn:hover {
  color: var(--color-text);
}

.error-banner {
  padding: 8px 16px;
  background: #fef2f2;
  color: #dc2626;
  font-size: 0.875rem;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 12px;
}

.my-message {
  align-self: flex-end;
  background: var(--color-accent);
  color: white;
}

.other-message {
  align-self: flex-start;
  background: var(--color-background-soft);
}

.system-message {
  align-self: center;
  background: transparent;
  padding: 4px 0;
}

.system-text {
  font-size: 0.75rem;
  color: var(--color-text-mute);
}

.message-header {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 4px;
}

.sender {
  font-weight: 600;
  font-size: 0.8rem;
}

.time {
  font-size: 0.7rem;
  opacity: 0.7;
}

.message-content {
  word-break: break-word;
  font-size: 0.9rem;
  line-height: 1.4;
}

.empty-state {
  text-align: center;
  color: var(--color-text-mute);
  padding: 40px 20px;
  font-size: 0.9rem;
}

.input-container {
  display: flex;
  gap: 8px;
  padding: 12px;
  background: var(--color-background-soft);
  border-top: 1px solid var(--color-border);
}

.input-container textarea {
  flex: 1;
  resize: none;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 0.9rem;
  min-height: 44px;
  max-height: 100px;
  background: var(--color-background);
}

.input-container textarea:focus {
  outline: none;
  border-color: var(--color-accent);
}

.send-btn {
  padding: 0 20px;
  background: var(--color-accent);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn:not(:disabled):hover {
  opacity: 0.9;
}
</style>
