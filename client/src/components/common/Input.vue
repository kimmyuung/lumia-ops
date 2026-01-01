<template>
  <input
    :type="type"
    :value="modelValue"
    :placeholder="placeholder"
    :disabled="disabled"
    :class="['input', { 'input-error': error }]"
    @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
  />
  <span v-if="error" class="input-error-message">{{ error }}</span>
</template>

<script setup lang="ts">
interface Props {
  modelValue: string
  type?: 'text' | 'email' | 'password' | 'number'
  placeholder?: string
  disabled?: boolean
  error?: string
}

withDefaults(defineProps<Props>(), {
  type: 'text',
  placeholder: '',
  disabled: false,
  error: ''
})

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<style scoped>
.input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition:
    border-color 0.2s,
    box-shadow 0.2s;
  outline: none;
}

.input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.input-error {
  border-color: #ef4444;
}

.input-error:focus {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.input-error-message {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.875rem;
  color: #ef4444;
}
</style>
