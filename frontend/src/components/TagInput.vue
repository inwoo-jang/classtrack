<script setup lang="ts">
import { computed, ref } from 'vue'

/**
 * 기술 같은 문자열 목록을 칩으로 입력받는다.
 *
 * <p>선택지를 고정하지 않는다 — 추천 목록은 표기 흔들림을 줄이기 위한 제안이고,
 * 목록에 없는 값도 자유롭게 넣을 수 있어야 한다. 과제에서 직접 파고든 기술이
 * 오히려 기록할 값어치가 크기 때문이다.
 */
const props = defineProps<{
  modelValue: string[]
  /** 전체 추천 목록 (설정 + 이미 쓰인 값) */
  options?: string[]
  /** 한 번에 담아올 수 있는 묶음 — 예: 이 강의에서 다룬 기술 */
  quickPick?: string[]
  quickPickLabel?: string
  placeholder?: string
  max?: number
}>()

const emit = defineEmits<{ 'update:modelValue': [string[]] }>()

const draft = ref('')

const full = computed(() => props.modelValue.length >= (props.max ?? 30))

/** 이미 담긴 것과 입력 중인 것을 뺀 추천 */
const suggestions = computed(() => {
  const chosen = new Set(props.modelValue)
  const needle = draft.value.trim().toLowerCase()
  return (props.options ?? [])
    .filter((o) => !chosen.has(o))
    .filter((o) => !needle || o.toLowerCase().includes(needle))
    .slice(0, 8)
})

const quickPickRemaining = computed(() => {
  const chosen = new Set(props.modelValue)
  return (props.quickPick ?? []).filter((q) => !chosen.has(q))
})

function add(value: string) {
  const next = value.trim()
  if (!next || full.value || props.modelValue.includes(next)) return
  emit('update:modelValue', [...props.modelValue, next])
  draft.value = ''
}

function remove(value: string) {
  emit('update:modelValue', props.modelValue.filter((v) => v !== value))
}

function addAllQuickPick() {
  const merged = [...props.modelValue]
  for (const q of quickPickRemaining.value) {
    if (merged.length >= (props.max ?? 30)) break
    merged.push(q)
  }
  emit('update:modelValue', merged)
}

/** Enter 로 추가, 빈 칸에서 Backspace 면 마지막 칩 삭제 */
function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter' || event.key === ',') {
    event.preventDefault()
    add(draft.value)
  } else if (event.key === 'Backspace' && !draft.value && props.modelValue.length) {
    remove(props.modelValue[props.modelValue.length - 1]!)
  }
}
</script>

<template>
  <div class="tags">
    <!-- 상위 강의에서 다룬 기술 — 클릭 한 번으로 복사 -->
    <div v-if="quickPickRemaining.length" class="quick">
      <span class="quick-label muted">
        {{ quickPickLabel ?? '이 강의에서 다룬 기술' }}
        <button type="button" class="all" @click="addAllQuickPick">전부 담기</button>
      </span>
      <div class="quick-chips">
        <button
          v-for="q in quickPickRemaining"
          :key="q"
          type="button"
          class="chip pick"
          :disabled="full"
          @click="add(q)"
        >
          + {{ q }}
        </button>
      </div>
    </div>

    <div class="box" :class="{ full }">
      <span v-for="v in modelValue" :key="v" class="chip on">
        {{ v }}
        <button type="button" class="x" :aria-label="`${v} 제거`" @click="remove(v)">×</button>
      </span>

      <input
        v-model="draft"
        class="entry"
        :placeholder="full ? `최대 ${max ?? 30}개` : (placeholder ?? '입력 후 Enter')"
        :disabled="full"
        @keydown="onKeydown"
      />
    </div>

    <div v-if="suggestions.length && !full" class="suggest">
      <button
        v-for="s in suggestions"
        :key="s"
        type="button"
        class="chip pick"
        @click="add(s)"
      >
        + {{ s }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.tags {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.quick {
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: 8px 10px;
  border: 1px dashed var(--line-strong);
  border-radius: var(--r-sm);
}

.quick-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.72rem;
}

.all {
  padding: 0;
  border: 0;
  background: none;
  font-size: 0.72rem;
  color: var(--mint-deep);
  cursor: pointer;
}

.quick-chips,
.suggest {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.box {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 5px;
  padding: 6px 8px;
  border: 1px solid var(--line-strong);
  border-radius: var(--r-md);
  background: var(--surface);
}

.box:focus-within {
  border-color: var(--mint);
  box-shadow: 0 0 0 3px var(--mint-wash);
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 0.74rem;
  white-space: nowrap;
}

.chip.on {
  background: var(--surface-sunken);
  border: 1px solid var(--line);
  color: var(--ink);
}

.chip.pick {
  border: 1px solid var(--line-strong);
  background: transparent;
  color: var(--ink-muted);
  cursor: pointer;
  transition:
    border-color 0.16s var(--ease),
    color 0.16s var(--ease);
}

.chip.pick:hover:not(:disabled) {
  border-color: var(--mint);
  color: var(--mint-deep);
}

.x {
  padding: 0;
  border: 0;
  background: none;
  color: var(--ink-muted);
  font-size: 0.85rem;
  line-height: 1;
  cursor: pointer;
}

.x:hover {
  color: var(--danger);
}

.entry {
  flex: 1;
  min-width: 120px;
  border: 0;
  outline: none;
  background: transparent;
  font-size: 0.85rem;
  padding: 2px 0;
}
</style>
