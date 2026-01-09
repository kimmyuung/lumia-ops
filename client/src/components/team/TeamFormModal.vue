<template>
  <Modal v-model="isOpen" :title="isEdit ? '팀 수정' : '팀 생성'" max-width="480px">
    <form class="team-form" @submit.prevent="handleSubmit">
      <div class="form-field">
        <label for="teamName">팀 이름 *</label>
        <Input
          id="teamName"
          v-model="form.name"
          placeholder="팀 이름을 입력하세요"
          :error="errors.name"
          :disabled="isLoading"
        />
      </div>

      <div class="form-field">
        <label for="teamDescription">팀 설명</label>
        <textarea
          id="teamDescription"
          v-model="form.description"
          placeholder="팀 설명을 입력하세요"
          rows="4"
          :disabled="isLoading"
          class="textarea"
        />
      </div>
    </form>

    <template #footer>
      <Button variant="secondary" :disabled="isLoading" @click="close"> 취소 </Button>
      <Button variant="primary" :loading="isLoading" @click="handleSubmit">
        <Save :size="18" />
        <span>{{ isEdit ? '수정' : '생성' }}</span>
      </Button>
    </template>
  </Modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { Save } from 'lucide-vue-next'
import { Modal, Button, Input } from '@/components/common'
import { useTeamStore } from '@/stores/team'
import type { Team, CreateTeamRequest } from '@/types/team'

interface Props {
  team?: Team | null
}

const props = withDefaults(defineProps<Props>(), {
  team: null
})

const emit = defineEmits<{
  close: []
  saved: [team: Team]
}>()

const teamStore = useTeamStore()

const isOpen = ref(true)
const isEdit = ref(false)
const isLoading = ref(false)

const form = reactive({
  name: '',
  description: ''
})

const errors = reactive({
  name: ''
})

// 편집 모드 감지
watch(
  () => props.team,
  team => {
    if (team) {
      isEdit.value = true
      form.name = team.name
      form.description = team.description || ''
    } else {
      isEdit.value = false
      form.name = ''
      form.description = ''
    }
  },
  { immediate: true }
)

function validate(): boolean {
  errors.name = ''

  if (!form.name.trim()) {
    errors.name = '팀 이름을 입력해 주세요.'
    return false
  }

  if (form.name.length < 2) {
    errors.name = '팀 이름은 2자 이상이어야 합니다.'
    return false
  }

  return true
}

async function handleSubmit() {
  if (!validate()) return

  isLoading.value = true

  try {
    let result: Team | null

    if (isEdit.value && props.team) {
      result = await teamStore.updateTeam(props.team.id, {
        name: form.name,
        description: form.description || undefined
      })
    } else {
      result = await teamStore.createTeam({
        name: form.name,
        description: form.description || undefined
      } as CreateTeamRequest)
    }

    if (result) {
      emit('saved', result)
      close()
    }
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
.team-form {
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

.textarea:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
