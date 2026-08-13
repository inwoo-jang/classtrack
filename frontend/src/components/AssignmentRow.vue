<script setup lang="ts">
import { computed, ref } from 'vue'
import { assignmentApi } from '@/api/courses'
import { ApiError } from '@/api/client'
import type { Assignment, AssignmentStatus } from '@/types/api'
import { ASSIGNMENT_STATUSES } from '@/types/api'
import { STATUS_LABEL, deadlineBadge, formatDateTime, isDone } from '@/utils/format'
import SubmissionLink from './SubmissionLink.vue'

const props = defineProps<{
  assignment: Assignment
  /** 전체 과제 목록에서는 어느 강의의 과제인지 함께 보여준다. */
  showCourse?: boolean
}>()

const emit = defineEmits<{ updated: [Assignment]; removed: [number] }>()

const badge = computed(() => deadlineBadge(props.assignment))
const done = computed(() => isDone(props.assignment.status))

const saving = ref(false)
const error = ref<string | null>(null)

/** 링크 편집 상태 */
const editing = ref(false)
const draftUrl = ref('')

function openEditor() {
  draftUrl.value = props.assignment.submissionUrl ?? ''
  editing.value = true
}

async function save(status: AssignmentStatus, submissionUrl: string | null) {
  saving.value = true
  error.value = null
  try {
    const updated = await assignmentApi.update(props.assignment.courseId, props.assignment.id, {
      status,
      submissionUrl,
    })
    emit('updated', updated)
    editing.value = false
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '저장에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

function changeStatus(event: Event) {
  const status = (event.target as HTMLSelectElement).value as AssignmentStatus
  save(status, props.assignment.submissionUrl)
}

function saveLink() {
  save(props.assignment.status, draftUrl.value.trim() || null)
}

async function remove() {
  if (!window.confirm(`"${props.assignment.title}" 과제를 삭제할까요?`)) return

  saving.value = true
  error.value = null
  try {
    await assignmentApi.remove(props.assignment.courseId, props.assignment.id)
    emit('removed', props.assignment.id)
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '삭제에 실패했습니다.'
    saving.value = false
  }
}
</script>

<template>
  <li class="row" :class="{ done, saving }">
    <div class="lead">
      <select
        class="status"
        :class="`status--${assignment.status.toLowerCase()}`"
        :value="assignment.status"
        :disabled="saving"
        :aria-label="`${assignment.title} 상태`"
        @change="changeStatus"
      >
        <option v-for="s in ASSIGNMENT_STATUSES" :key="s" :value="s">
          {{ STATUS_LABEL[s] }}
        </option>
      </select>
    </div>

    <div class="main">
      <div class="line">
        <h4 class="name">{{ assignment.title }}</h4>
        <SubmissionLink
          v-if="assignment.submissionUrl"
          :url="assignment.submissionUrl"
          :status="assignment.linkStatus"
        />
        <button class="edit" type="button" @click="openEditor">
          {{ assignment.submissionUrl ? '수정' : '+ 결과물 링크' }}
        </button>
        <button class="edit danger" type="button" @click="remove">삭제</button>
      </div>

      <RouterLink v-if="showCourse" :to="`/courses/${assignment.courseId}`" class="course">
        {{ assignment.courseTitle }}
      </RouterLink>

      <p v-if="assignment.description" class="desc muted">{{ assignment.description }}</p>

      <form v-if="editing" class="editor" @submit.prevent="saveLink">
        <input
          v-model="draftUrl"
          class="input"
          type="url"
          placeholder="https://github.com/… 또는 https://drive.google.com/…"
          autofocus
        />
        <button class="btn btn-primary btn-sm" type="submit" :disabled="saving">저장</button>
        <button class="btn btn-ghost btn-sm" type="button" @click="editing = false">취소</button>
      </form>

      <p v-if="error" class="err">{{ error }}</p>
    </div>

    <div class="due">
      <time :datetime="assignment.dueDate">{{ formatDateTime(assignment.dueDate) }}</time>
      <span v-if="badge" class="dday" :class="{ urgent: badge.urgent }">{{ badge.text }}</span>
    </div>
  </li>
</template>

<style scoped>
.row {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: start;
  gap: 16px;
  padding: 16px 22px;
  border-top: 1px solid var(--line);
  transition:
    background 0.16s var(--ease),
    opacity 0.16s var(--ease);
}

.row:hover {
  background: var(--surface-sunken);
}

.row.saving {
  opacity: 0.6;
  pointer-events: none;
}

.lead {
  padding-top: 1px;
}

/* 상태 선택 — 배지처럼 보이지만 실제로는 select */
.status {
  appearance: none;
  padding: 3px 22px 3px 10px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 0.74rem;
  font-weight: 500;
  cursor: pointer;
  background-image: linear-gradient(45deg, transparent 50%, currentColor 50%),
    linear-gradient(135deg, currentColor 50%, transparent 50%);
  background-position:
    calc(100% - 11px) calc(50% + 1px),
    calc(100% - 8px) calc(50% + 1px);
  background-size: 3px 3px;
  background-repeat: no-repeat;
  transition: border-color 0.16s var(--ease);
}

.status:hover {
  border-color: currentColor;
}

.status--todo {
  background-color: var(--surface-sunken);
  color: var(--ink-muted);
}

.status--in_progress {
  background-color: var(--amber-wash);
  color: var(--amber-ink);
}

.status--completed {
  background-color: var(--mint-wash);
  color: var(--mint-deep);
}


.main {
  min-width: 0;
}

.line {
  display: flex;
  align-items: center;
  gap: 9px;
  flex-wrap: wrap;
}

.name {
  margin: 0;
  font-size: 0.94rem;
  font-weight: 500;
  letter-spacing: -0.012em;
}

.row.done .name {
  color: var(--ink-soft);
}

.course {
  display: inline-block;
  margin-top: 3px;
  font-size: 0.78rem;
  color: var(--ink-muted);
  transition: color 0.16s var(--ease);
}

.course:hover {
  color: var(--mint-deep);
}

.desc {
  margin: 4px 0 0;
  font-size: 0.83rem;
  overflow-wrap: anywhere;
}

.edit {
  padding: 0;
  border: 0;
  background: none;
  font-size: 0.74rem;
  color: var(--ink-muted);
  cursor: pointer;
  opacity: 0;
  transition:
    opacity 0.16s var(--ease),
    color 0.16s var(--ease);
}

.row:hover .edit,
.edit:focus-visible {
  opacity: 1;
}

.edit:hover {
  color: var(--mint-deep);
}

.edit.danger:hover {
  color: var(--danger);
}

.editor {
  display: flex;
  gap: 7px;
  margin-top: 9px;
  flex-wrap: wrap;
}

.editor .input {
  flex: 1;
  min-width: 200px;
  padding: 6px 11px;
  font-size: 0.83rem;
}

.btn-sm {
  padding: 6px 14px;
  font-size: 0.8rem;
}

.err {
  margin: 7px 0 0;
  font-size: 0.78rem;
  color: var(--danger);
}

.due {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  font-size: 0.8rem;
  color: var(--ink-soft);
  text-align: right;
  white-space: nowrap;
}

.dday {
  font-size: 0.73rem;
  font-weight: 600;
  color: var(--ink-muted);
}

.dday.urgent {
  color: var(--danger);
}

@media (max-width: 680px) {
  .row {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .due {
    grid-column: 2;
    align-items: flex-start;
    text-align: left;
    flex-direction: row;
    gap: 8px;
    margin-top: 2px;
  }
}
</style>
