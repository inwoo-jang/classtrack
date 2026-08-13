<script setup lang="ts">
import type { AssignmentStatus } from '@/types/api'
import { STATUS_LABEL } from '@/utils/format'

defineProps<{ status: AssignmentStatus }>()
</script>

<template>
  <span class="pill" :class="`pill--${status.toLowerCase()}`">
    {{ STATUS_LABEL[status] }}
  </span>
</template>

<style scoped>
.pill {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 0.74rem;
  font-weight: 500;
  letter-spacing: 0.01em;
  white-space: nowrap;
}

.pill--todo {
  background: var(--surface-sunken);
  color: var(--ink-muted);
}

.pill--in_progress {
  background: var(--amber-wash);
  color: var(--amber-ink);
}

.pill--completed {
  background: var(--mint-wash);
  color: var(--mint-deep);
}

</style>
