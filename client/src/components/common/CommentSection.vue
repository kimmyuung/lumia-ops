<template>
  <div class="comment-section">
    <div class="section-header">
      <h4>💬 코멘트 ({{ comments.length }})</h4>
    </div>

    <!-- 코멘트 입력 -->
    <div class="comment-input">
      <textarea
        v-model="newComment"
        placeholder="코멘트를 입력하세요..."
        :disabled="isSubmitting"
        @keydown.ctrl.enter="handleSubmit"
      ></textarea>
      <button 
        class="submit-btn" 
        :disabled="!newComment.trim() || isSubmitting"
        @click="handleSubmit"
      >
        {{ isSubmitting ? '전송 중...' : '등록' }}
      </button>
    </div>

    <!-- 코멘트 목록 -->
    <div class="comment-list">
      <div
        v-for="comment in comments"
        :key="comment.id"
        class="comment-item"
      >
        <div class="comment-header">
          <span class="author">{{ comment.userName }}</span>
          <span class="time">{{ formatTime(comment.createdAt) }}</span>
        </div>
        
        <div v-if="editingId === comment.id" class="edit-mode">
          <textarea v-model="editContent"></textarea>
          <div class="edit-actions">
            <button class="save-btn" @click="handleUpdate(comment.id)">저장</button>
            <button class="cancel-btn" @click="cancelEdit">취소</button>
          </div>
        </div>
        
        <template v-else>
          <p class="comment-content">{{ comment.content }}</p>
          <div v-if="comment.userId === currentUserId" class="comment-actions">
            <button @click="startEdit(comment)">수정</button>
            <button @click="handleDelete(comment.id)">삭제</button>
          </div>
        </template>
      </div>

      <div v-if="comments.length === 0 && !isLoading" class="empty-state">
        <p>아직 코멘트가 없습니다. 첫 코멘트를 남겨보세요!</p>
      </div>

      <div v-if="isLoading" class="loading-state">
        <p>불러오는 중...</p>
      </div>
    </div>

    <!-- 에러 메시지 -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { commentApi, type CommentResponse, type CommentTargetType } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import { getErrorMessage } from '@/utils/error'

const props = defineProps<{
  targetType: CommentTargetType
  targetId: number
}>()

const userStore = useUserStore()
const currentUserId = Number(userStore.user?.id)

const comments = ref<CommentResponse[]>([])
const newComment = ref('')
const editingId = ref<number | null>(null)
const editContent = ref('')
const isLoading = ref(false)
const isSubmitting = ref(false)
const error = ref<string | null>(null)

async function fetchComments() {
  isLoading.value = true
  error.value = null
  try {
    comments.value = await commentApi.getComments(props.targetType, props.targetId)
  } catch (err) {
    error.value = getErrorMessage(err, '코멘트를 불러오지 못했습니다.')
  } finally {
    isLoading.value = false
  }
}

async function handleSubmit() {
  if (!newComment.value.trim()) return
  
  isSubmitting.value = true
  error.value = null
  try {
    const comment = await commentApi.createComment(
      props.targetType,
      props.targetId,
      newComment.value.trim()
    )
    comments.value.unshift(comment)
    newComment.value = ''
  } catch (err) {
    error.value = getErrorMessage(err, '코멘트 등록에 실패했습니다.')
  } finally {
    isSubmitting.value = false
  }
}

function startEdit(comment: CommentResponse) {
  editingId.value = comment.id
  editContent.value = comment.content
}

function cancelEdit() {
  editingId.value = null
  editContent.value = ''
}

async function handleUpdate(commentId: number) {
  if (!editContent.value.trim()) return
  
  try {
    const updated = await commentApi.updateComment(commentId, editContent.value.trim())
    const index = comments.value.findIndex(c => c.id === commentId)
    if (index !== -1) {
      comments.value[index] = updated
    }
    cancelEdit()
  } catch (err) {
    error.value = getErrorMessage(err, '수정에 실패했습니다.')
  }
}

async function handleDelete(commentId: number) {
  if (!confirm('코멘트를 삭제하시겠습니까?')) return
  
  try {
    await commentApi.deleteComment(commentId)
    comments.value = comments.value.filter(c => c.id !== commentId)
  } catch (err) {
    error.value = getErrorMessage(err, '삭제에 실패했습니다.')
  }
}

function formatTime(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comment-section {
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  border: 1px solid var(--border-color);
}

.section-header h4 {
  margin: 0 0 1rem;
  font-size: 1rem;
}

.comment-input {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.comment-input textarea {
  flex: 1;
  min-height: 60px;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  resize: vertical;
  font-family: inherit;
}

.comment-input textarea:focus {
  outline: none;
  border-color: var(--primary-color);
}

.submit-btn {
  padding: 0 1.5rem;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-weight: 500;
  cursor: pointer;
  align-self: flex-end;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.comment-item {
  padding: 1rem;
  background: var(--bg-color-alt);
  border-radius: var(--radius-md);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.author {
  font-weight: 600;
  font-size: 0.875rem;
}

.time {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.comment-content {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.5;
  white-space: pre-wrap;
}

.comment-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.comment-actions button {
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 0.75rem;
  cursor: pointer;
}

.comment-actions button:hover {
  color: var(--primary-color);
}

.edit-mode textarea {
  width: 100%;
  min-height: 60px;
  padding: 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  margin-bottom: 0.5rem;
}

.edit-actions {
  display: flex;
  gap: 0.5rem;
}

.save-btn, .cancel-btn {
  padding: 0.25rem 0.75rem;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 0.8rem;
}

.save-btn {
  background: var(--primary-color);
  color: white;
}

.cancel-btn {
  background: var(--bg-color);
  color: var(--text-muted);
}

.empty-state, .loading-state {
  text-align: center;
  padding: 2rem;
  color: var(--text-muted);
}

.error-message {
  margin-top: 1rem;
  padding: 0.75rem;
  background: rgba(var(--error-rgb), 0.1);
  color: var(--error-color);
  border-radius: var(--radius-md);
  font-size: 0.875rem;
}
</style>
