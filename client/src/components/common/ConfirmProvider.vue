<template>
  <ConfirmModal
    :is-open="state.isOpen"
    :title="state.title"
    :message="state.message"
    :confirm-text="state.confirmText"
    :cancel-text="state.cancelText"
    :variant="state.variant"
    :is-loading="state.isLoading"
    @confirm="handleConfirm"
    @cancel="handleCancel"
  />
</template>

<script setup lang="ts">
import ConfirmModal from './ConfirmModal.vue'
import { useConfirm } from '@/composables/useConfirm'

const { state, handleConfirm, handleCancel } = useConfirm()
</script>
