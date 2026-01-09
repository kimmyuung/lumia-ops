<template>
  <Modal v-model="isOpen" title="팀원 초대" max-width="480px">
    <form class="invite-form" @submit.prevent="handleSubmit">
      <div class="form-field">
        <label for="email">이메일 주소 *</label>
        <Input
          id="email"
          v-model="form.email"
          type="email"
          placeholder="초대할 사용자의 이메일을 입력하세요"
          :error="errors.email"
          :disabled="isLoading"
        />
      </div>

      <div class="form-field">
        <label for="role">역할</label>
        <select id="role" v-model="form.role" class="select-input" :disabled="isLoading">
          <option value="MEMBER">멤버</option>
          <option value="ADMIN">관리자</option>
        </select>
      </div>

      <div class="form-field">
        <label for="message">초대 메시지 (선택)</label>
        <textarea
          id="message"
          v-model="form.message"
          placeholder="초대 메시지를 입력하세요"
          rows="3"
          :disabled="isLoading"
          class="textarea"
        />
      </div>

      <div class="info-box">
        <Mail :size="18" />
        <p>초대 이메일이 해당 주소로 발송됩니다. 상대방이 초대를 수락해야 팀원으로 등록됩니다.</p>
      </div>
    </form>

    <template #footer>
      <Button variant="secondary" :disabled="isLoading" @click="close"> 취소 </Button>
      <Button variant="primary" :loading="isLoading" @click="handleSubmit">
        <Send :size="18" />
        <span>초대 발송</span>
      </Button>
    </template>
  </Modal>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Mail, Send } from 'lucide-vue-next'
import { Modal, Button, Input } from '@/components/common'
import { invitationApi } from '@/api/invitation'
import { useToast } from '@/composables/useToast'

interface Props {
  teamId: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  close: []
  invited: []
}>()

const toast = useToast()

const isOpen = ref(true)
const isLoading = ref(false)

const form = reactive({
  email: '',
  role: 'MEMBER' as 'ADMIN' | 'MEMBER',
  message: ''
})

const errors = reactive({
  email: ''
})

function validate(): boolean {
  errors.email = ''

  if (!form.email.trim()) {
    errors.email = '이메일을 입력해 주세요.'
    return false
  }

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = '올바른 이메일 형식이 아닙니다.'
    return false
  }

  return true
}

async function handleSubmit() {
  if (!validate()) return

  isLoading.value = true

  try {
    const result = await invitationApi.createInvitation(props.teamId, {
      email: form.email,
      role: form.role,
      message: form.message || undefined
    })

    if (result.emailSent) {
      toast.success(`${form.email}로 초대 이메일을 발송했습니다.`)
    } else {
      toast.warning('초대가 생성되었지만 이메일 발송에 실패했습니다.')
    }

    emit('invited')
    close()
  } catch (err: unknown) {
    const e = err as { message?: string }
    toast.error(e.message || '초대 발송에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

function close() {
  isOpen.value = false
  emit('close')
}
</script>

<style scoped>
.invite-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-field label {
  font-weight: 500;
  color: var(--text-color);
  font-size: 0.9rem;
}

.select-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 1rem;
  font-family: inherit;
  background: var(--card-bg-solid);
  color: var(--text-color);
  cursor: pointer;
  transition: border-color var(--transition-fast);
}

.select-input:focus {
  outline: none;
  border-color: var(--primary-color);
}

.textarea {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 1rem;
  font-family: inherit;
  resize: vertical;
  background: var(--card-bg-solid);
  color: var(--text-color);
  transition: border-color var(--transition-fast);
}

.textarea:focus {
  outline: none;
  border-color: var(--primary-color);
}

.info-box {
  display: flex;
  gap: 0.75rem;
  padding: 1rem;
  background: rgba(59, 130, 246, 0.1);
  border-radius: var(--radius-md);
  color: var(--info-color);
}

.info-box p {
  font-size: 0.875rem;
  line-height: 1.5;
  margin: 0;
}
</style>
