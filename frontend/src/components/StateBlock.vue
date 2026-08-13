<script setup lang="ts">
/** 로딩 / 에러 / 비어 있음 상태를 한 곳에서 처리하는 래퍼. */
defineProps<{
  loading: boolean
  error: string | null
  empty?: boolean
  emptyText?: string
}>()
</script>

<template>
  <div v-if="loading" class="state">
    <span class="spinner" aria-hidden="true" />
    <span class="muted">불러오는 중…</span>
  </div>

  <p v-else-if="error" class="notice notice-error">{{ error }}</p>

  <div v-else-if="empty" class="state">
    <span class="muted">{{ emptyText ?? '아직 등록된 항목이 없습니다.' }}</span>
  </div>

  <slot v-else />
</template>

<style scoped>
.state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 64px 24px;
  border: 1px dashed var(--line-strong);
  border-radius: var(--r-lg);
  font-size: 0.9rem;
}

.spinner {
  width: 15px;
  height: 15px;
  border: 2px solid var(--line-strong);
  border-top-color: var(--mint);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (prefers-reduced-motion: reduce) {
  .spinner {
    animation-duration: 2s;
  }
}
</style>
