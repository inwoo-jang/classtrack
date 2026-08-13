<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { assignmentApi } from '@/api/courses'
import { ApiError } from '@/api/client'
import type { Assignment, AssignmentStatus } from '@/types/api'
import { ASSIGNMENT_STATUSES } from '@/types/api'
import { STATUS_LABEL, daysUntil, isDone } from '@/utils/format'
import AssignmentRow from '@/components/AssignmentRow.vue'
import StateBlock from '@/components/StateBlock.vue'

const assignments = ref<Assignment[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

type Filter = 'ALL' | 'OPEN' | AssignmentStatus
const filter = ref<Filter>('OPEN')

const FILTERS: { key: Filter; label: string }[] = [
  { key: 'OPEN', label: '남은 과제' },
  { key: 'ALL', label: '전체' },
  ...ASSIGNMENT_STATUSES.map((s) => ({ key: s as Filter, label: STATUS_LABEL[s] })),
]

function matches(assignment: Assignment, key: Filter): boolean {
  if (key === 'ALL') return true
  if (key === 'OPEN') return !isDone(assignment.status)
  return assignment.status === key
}

const counts = computed(() =>
  Object.fromEntries(
    FILTERS.map((f) => [f.key, assignments.value.filter((a) => matches(a, f.key)).length]),
  ),
)

const visible = computed(() => assignments.value.filter((a) => matches(a, filter.value)))

const overdueCount = computed(
  () => assignments.value.filter((a) => !isDone(a.status) && daysUntil(a.dueDate) < 0).length,
)

/** 수정된 과제를 목록에 반영한다 (전체 재조회 없이). */
function onUpdated(updated: Assignment) {
  const i = assignments.value.findIndex((a) => a.id === updated.id)
  if (i !== -1) assignments.value[i] = updated
}

function onRemoved(id: number) {
  assignments.value = assignments.value.filter((a) => a.id !== id)
}

onMounted(async () => {
  try {
    assignments.value = await assignmentApi.listAll()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '알 수 없는 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <header class="page-head">
    <div>
      <h1 class="page-title">과제</h1>
      <p class="muted page-sub">
        <template v-if="!loading && !error">
          전체 {{ assignments.length }}개<template v-if="overdueCount">
            · <span class="alert">마감 지남 {{ overdueCount }}</span>
          </template>
        </template>
        <template v-else>&nbsp;</template>
      </p>
    </div>
  </header>

  <nav v-if="!loading && !error && assignments.length" class="filters">
    <button
      v-for="f in FILTERS"
      :key="f.key"
      class="chip"
      :class="{ on: filter === f.key }"
      type="button"
      @click="filter = f.key"
    >
      {{ f.label }}
      <span class="n">{{ counts[f.key] }}</span>
    </button>
  </nav>

  <StateBlock
    :loading="loading"
    :error="error"
    :empty="assignments.length === 0"
    empty-text="아직 등록된 과제가 없습니다. 강의 상세 화면에서 과제를 추가하세요."
  >
    <div class="card list-wrap">
      <ul v-if="visible.length" class="list">
        <AssignmentRow
          v-for="assignment in visible"
          :key="assignment.id"
          :assignment="assignment"
          show-course
          @updated="onUpdated"
          @removed="onRemoved"
        />
      </ul>
      <p v-else class="empty muted">이 조건에 해당하는 과제가 없습니다.</p>
    </div>
  </StateBlock>
</template>

<style scoped>
.page-head {
  padding: 44px 0 20px;
}

.page-title {
  font-size: 1.85rem;
  font-weight: 500;
  letter-spacing: -0.03em;
  line-height: 1.2;
}

.page-sub {
  margin-top: 3px;
  font-size: 0.85rem;
}

.alert {
  color: var(--danger);
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-bottom: 20px;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 14px;
  border: 1px solid var(--line-strong);
  border-radius: 999px;
  background: transparent;
  font-size: 0.83rem;
  color: var(--ink-soft);
  cursor: pointer;
  transition:
    background 0.16s var(--ease),
    border-color 0.16s var(--ease),
    color 0.16s var(--ease);
}

.chip:hover {
  background: var(--surface);
  border-color: var(--ink-muted);
}

.chip.on {
  background: var(--mint-ink);
  border-color: var(--mint-ink);
  color: #fff;
}

.n {
  font-size: 0.75rem;
  font-variant-numeric: tabular-nums;
  opacity: 0.65;
}

.list-wrap {
  overflow: hidden;
  margin-bottom: 72px;
}

.list {
  list-style: none;
  margin: 0;
  padding: 0;
}

/* 첫 행의 상단 보더는 카드 테두리와 겹치므로 지운다 */
.list > :first-child {
  border-top: 0;
}

.empty {
  padding: 52px 24px;
  text-align: center;
  font-size: 0.9rem;
}
</style>
