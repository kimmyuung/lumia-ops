<template>
  <Modal v-model="isOpen" :title="isEdit ? '스크림 수정' : '스크림 생성'" max-width="520px">
    <form class="scrim-form" @submit.prevent="handleSubmit">
      <div class="form-field">
        <label for="scrimTitle">제목 *</label>
        <Input
          id="scrimTitle"
          v-model="form.title"
          placeholder="스크림 제목을 입력하세요"
          :error="errors.title"
          :disabled="isLoading"
        />
      </div>

      <div class="form-row">
        <div class="form-field">
          <label for="scheduledAt">예정 일시 *</label>
          <input
            id="scheduledAt"
            v-model="form.scheduledAt"
            type="datetime-local"
            class="datetime-input"
            :disabled="isLoading"
          />
          <span v-if="errors.scheduledAt" class="error-text">{{ errors.scheduledAt }}</span>
        </div>

        <div class="form-field">
          <label for="opponentTeamName">상대 팀</label>
          <Input
            id="opponentTeamName"
            v-model="form.opponentTeamName"
            placeholder="상대 팀 이름"
            :disabled="isLoading"
          />
        </div>
      </div>

      <div class="form-field">
        <label for="mapName">맵</label>
        <Input
          id="mapName"
          v-model="form.mapName"
          placeholder="맵 이름 (선택)"
          :disabled="isLoading"
        />
      </div>

      <div class="form-field">
        <label for="notes">메모</label>
        <textarea
          id="notes"
          v-model="form.notes"
          placeholder="메모 (선택)"
          rows="3"
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
import { useScrimStore } from '@/stores/scrim'
import type { Scrim, CreateScrimRequest } from '@/types/scrim'

interface Props {
  scrim?: Scrim | null
}

const props = withDefaults(defineProps<Props>(), {
  scrim: null
})

const emit = defineEmits<{
  close: []
  saved: [scrim: Scrim]
}>()

const scrimStore = useScrimStore()

const isOpen = ref(true)
const isEdit = ref(false)
const isLoading = ref(false)

const form = reactive({
  title: '',
  scheduledAt: '',
  opponentTeamName: '',
  mapName: '',
  notes: ''
})

const errors = reactive({
  title: '',
  scheduledAt: ''
})

// 편집 모드 감지
watch(
  () => props.scrim,
  scrim => {
    if (scrim) {
      isEdit.value = true
      form.title = scrim.title
      form.scheduledAt = scrim.scheduledAt.slice(0, 16) // datetime-local 형식
      form.opponentTeamName = scrim.opponentTeamName || ''
      form.mapName = scrim.mapName || ''
      form.notes = scrim.notes || ''
    } else {
      isEdit.value = false
      form.title = ''
      form.scheduledAt = ''
      form.opponentTeamName = ''
      form.mapName = ''
      form.notes = ''
    }
  },
  { immediate: true }
)

function validate(): boolean {
  errors.title = ''
  errors.scheduledAt = ''
  let valid = true

  if (!form.title.trim()) {
    errors.title = '제목을 입력해 주세요.'
    valid = false
  }

  if (!form.scheduledAt) {
    errors.scheduledAt = '예정 일시를 선택해 주세요.'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  if (!validate()) return

  isLoading.value = true

  try {
    const data = {
      title: form.title,
      scheduledAt: new Date(form.scheduledAt).toISOString(),
      opponentTeamName: form.opponentTeamName || undefined,
      mapName: form.mapName || undefined,
      notes: form.notes || undefined
    }

    let result: Scrim | null

    if (isEdit.value && props.scrim) {
      result = await scrimStore.updateScrim(props.scrim.id, data)
    } else {
      result = await scrimStore.createScrim(data as CreateScrimRequest)
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
.scrim-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
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

.datetime-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 1rem;
  font-family: inherit;
  background: var(--card-bg-solid);
  color: var(--text-color);
  transition: border-color var(--transition-fast);
}

.datetime-input:focus {
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

.error-text {
  font-size: 0.875rem;
  color: var(--error-color);
}

@media (max-width: 480px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
