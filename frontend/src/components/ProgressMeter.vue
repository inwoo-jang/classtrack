<script setup lang="ts">
import { computed } from 'vue'
import type { AssignmentSummary } from '@/types/api'

const props = defineProps<{ summary: AssignmentSummary }>()

/** 진행 전 / 진행 중 / 완료 세 구간을 비율로 쌓아 보여준다. */
const segments = computed(() => {
  const { total, todo, inProgress, completed } = props.summary
  if (total === 0) return []

  const pct = (n: number) => (n / total) * 100
  return [
    { key: 'done', width: pct(completed) },
    { key: 'progress', width: pct(inProgress) },
    { key: 'todo', width: pct(todo) },
  ].filter((s) => s.width > 0)
})
</script>

<template>
  <div class="meter" :class="{ blank: segments.length === 0 }" role="presentation">
    <span
      v-for="segment in segments"
      :key="segment.key"
      class="seg"
      :class="`seg--${segment.key}`"
      :style="{ width: `${segment.width}%` }"
    />
  </div>
</template>

<style scoped>
.meter {
  display: flex;
  height: 4px;
  border-radius: 999px;
  overflow: hidden;
  background: var(--surface-sunken);
}

.meter.blank {
  background: repeating-linear-gradient(
    90deg,
    var(--surface-sunken) 0 6px,
    transparent 6px 12px
  );
}

.seg {
  display: block;
  height: 100%;
  transition: width 0.4s var(--ease);
}

.seg--done {
  background: var(--ink);
}

.seg--progress {
  background: var(--mint);
}

.seg--todo {
  background: var(--line-strong);
}
</style>
