<script setup lang="ts">
import { computed } from 'vue'
import type { LinkStatus } from '@/types/api'
import { linkKind } from '@/utils/format'

const props = defineProps<{ url: string; status?: LinkStatus }>()

const kind = computed(() => linkKind(props.url))

/** 비동기 검증 결과. PENDING 은 아직 확인 중이라는 뜻. */
const mark = computed(() => {
  switch (props.status) {
    case 'OK':
      return { char: '✓', title: '링크 확인됨', cls: 'ok' }
    case 'BROKEN':
      return { char: '!', title: '열리지 않는 링크', cls: 'broken' }
    case 'PENDING':
      return { char: '⋯', title: '확인 중', cls: 'pending' }
    default:
      return null
  }
})
</script>

<template>
  <a class="link" :class="status ? `is-${status.toLowerCase()}` : ''" :href="url" target="_blank" rel="noopener noreferrer">
    <svg viewBox="0 0 16 16" width="12" height="12" aria-hidden="true">
      <path
        d="M6.5 9.5 9.5 6.5M7 4.5 8.2 3.3a2.4 2.4 0 0 1 3.4 3.4L10.4 7.9M9 11.5 7.8 12.7a2.4 2.4 0 0 1-3.4-3.4L5.6 8.1"
        fill="none"
        stroke="currentColor"
        stroke-width="1.4"
        stroke-linecap="round"
      />
    </svg>
    {{ kind }}
    <span v-if="mark" class="mark" :class="mark.cls" :title="mark.title">{{ mark.char }}</span>
  </a>
</template>

<style scoped>
.link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 2px 9px 2px 7px;
  border: 1px solid var(--line-strong);
  border-radius: 999px;
  font-size: 0.74rem;
  color: var(--ink-soft);
  transition:
    border-color 0.16s var(--ease),
    color 0.16s var(--ease),
    background 0.16s var(--ease);
}

.mark {
  font-size: 0.7rem;
  line-height: 1;
}

.mark.ok {
  color: var(--mint-deep);
}

.mark.broken {
  color: var(--danger);
}

.mark.pending {
  color: var(--ink-muted);
}

.link.is-broken {
  border-color: #e8d3d4;
}

.link:hover {
  border-color: var(--mint);
  background: var(--mint-wash);
  color: var(--mint-ink);
}
</style>
