<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { assignmentApi } from '@/api/courses'
import { ApiError } from '@/api/client'
import type { Assignment, AssignmentMode, AssignmentRequirement, AssignmentStatus } from '@/types/api'
import { ASSIGNMENT_STATUSES } from '@/types/api'
import { STATUS_LABEL, deadlineBadge, formatDateTime, isDone } from '@/utils/format'
import SubmissionLink from './SubmissionLink.vue'
import TagInput from './TagInput.vue'

const props = defineProps<{
  assignment: Assignment
  showCourse?: boolean
  /** 상위 강의에서 다룬 기술. 편집 시 한 번에 담아올 수 있게 전달한다 (강의 상세에서만 제공) */
  courseTechnologies?: string[]
  /** 전체 기술 추천 목록 */
  technologyOptions?: string[]
}>()
const emit = defineEmits<{ updated: [Assignment]; removed: [number] }>()

const badge = computed(() => deadlineBadge(props.assignment))
const done = computed(() => isDone(props.assignment.status))
const saving = ref(false)
const error = ref<string | null>(null)
const editing = ref(false)
const draft = reactive({
  title: '', description: '', dueDate: '', noDueDate: false,
  assignmentMode: 'INDIVIDUAL' as AssignmentMode,
  requirement: 'REQUIRED' as AssignmentRequirement,
  submissionUrlsText: '',
  technologies: [] as string[],
  featured: false,
  teamSize: null as number | null,
})

function openEditor() {
  const a = props.assignment
  draft.title = a.title
  draft.description = a.description ?? ''
  draft.dueDate = a.dueDate?.slice(0, 16) ?? ''
  draft.noDueDate = !a.dueDate
  draft.assignmentMode = a.assignmentMode
  draft.requirement = a.requirement
  draft.submissionUrlsText = a.submissionLinks.map((link) => link.url).join('\n')
  draft.technologies = [...a.technologies]
  draft.featured = a.featured
  draft.teamSize = a.teamSize
  editing.value = true
}

function body(status: AssignmentStatus) {
  return {
    title: draft.title,
    description: draft.description || null,
    dueDate: draft.noDueDate ? null : draft.dueDate.length === 16 ? `${draft.dueDate}:00` : draft.dueDate || null,
    assignmentMode: draft.assignmentMode,
    requirement: draft.requirement,
    status,
    submissionUrls: draft.submissionUrlsText.split('\n').map((url) => url.trim()).filter(Boolean),
    technologies: draft.technologies,
    featured: draft.featured,
    // 개인 과제로 바꾸면 서버가 알아서 지우지만, 보내는 값도 맞춰둔다
    teamSize: draft.assignmentMode === 'TEAM' ? draft.teamSize : null,
  }
}

async function save(status: AssignmentStatus, closeEditor = false) {
  saving.value = true
  error.value = null
  try {
    if (!editing.value) openEditor()
    const updated = await assignmentApi.update(props.assignment.courseId, props.assignment.id, body(status))
    emit('updated', updated)
    if (closeEditor) editing.value = false
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '저장에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

function changeStatus(event: Event) {
  save((event.target as HTMLSelectElement).value as AssignmentStatus)
}

async function remove() {
  // 강의 삭제와 같은 무게로 다룬다 — 되돌릴 수 없다는 점을 분명히 알린다.
  const message =
    `"${props.assignment.title}" 과제를 삭제할까요?\n` +
    '등록한 결과물 링크와 진행 상태가 함께 사라지며 되돌릴 수 없습니다.'
  if (!window.confirm(message)) return
  saving.value = true
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
    <select class="status" :value="assignment.status" :disabled="saving" @change="changeStatus">
      <option v-for="s in ASSIGNMENT_STATUSES" :key="s" :value="s">{{ STATUS_LABEL[s] }}</option>
    </select>

    <div class="main">
      <div class="line">
        <h4 class="name">{{ assignment.title }}</h4>
        <span class="tag">{{ assignment.assignmentMode === 'TEAM' ? '팀' : '개인' }}</span>
        <span class="tag">{{ assignment.requirement === 'REQUIRED' ? '필수' : '자율' }}</span>
        <span v-if="assignment.featured" class="star" title="대표 과제">★</span>
        <SubmissionLink v-for="link in assignment.submissionLinks" :key="link.url" :url="link.url" :status="link.status" />
        <button class="edit" type="button" @click="openEditor">수정</button>
      </div>

      <RouterLink v-if="showCourse" :to="`/courses/${assignment.courseId}`" class="course">{{ assignment.courseTitle }}</RouterLink>
      <p v-if="assignment.description" class="desc muted">{{ assignment.description }}</p>

      <p v-if="assignment.technologies.length" class="techs">
        <span v-for="tech in assignment.technologies" :key="tech" class="tech">{{ tech }}</span>
      </p>

      <form v-if="editing" class="editor" @submit.prevent="save(assignment.status, true)">
        <input v-model="draft.title" class="input wide" required placeholder="과제명" />
        <textarea v-model="draft.description" class="input wide" rows="4" maxlength="5000" placeholder="설명 · 최대 10줄" />
        <div class="fields">
          <select v-model="draft.assignmentMode" class="input"><option value="INDIVIDUAL">개인</option><option value="TEAM">팀</option></select>
          <select v-model="draft.requirement" class="input"><option value="REQUIRED">필수</option><option value="OPTIONAL">자율</option></select>
          <input v-model="draft.dueDate" class="input" type="datetime-local" :disabled="draft.noDueDate" />
          <label><input v-model="draft.noDueDate" type="checkbox" /> 마감 없음</label>
        </div>
        <textarea v-model="draft.submissionUrlsText" class="input wide" rows="3" placeholder="결과물 링크를 한 줄에 하나씩 입력 (최대 10개)" />

        <label v-if="draft.assignmentMode === 'TEAM'" class="team">
          팀 규모
          <input v-model.number="draft.teamSize" class="input team-size" type="number" min="2" placeholder="명" />
          <span class="muted">명</span>
        </label>

        <div class="tech-field">
          <span class="tech-label muted">이 과제에서 사용한 기술</span>
          <TagInput
            v-model="draft.technologies"
            :options="technologyOptions"
            :quick-pick="courseTechnologies"
            placeholder="직접 쓴 기술 · 입력 후 Enter"
          />
        </div>

        <label class="featured-toggle">
          <input v-model="draft.featured" type="checkbox" />
          <span>대표 과제 — 포트폴리오로 내보낼 때 포함</span>
        </label>
        <div class="actions">
          <button class="btn btn-danger btn-sm" type="button" :disabled="saving" @click="remove">
            삭제
          </button>
          <span class="spacer" />
          <button class="btn btn-ghost btn-sm" type="button" @click="editing = false">취소</button>
          <button class="btn btn-primary btn-sm" :disabled="saving">저장</button>
        </div>
      </form>
      <p v-if="error" class="err">{{ error }}</p>
    </div>

    <div class="due">
      <time v-if="assignment.dueDate" :datetime="assignment.dueDate">{{ formatDateTime(assignment.dueDate) }}</time>
      <span v-else>마감 없음</span>
      <span v-if="badge" class="dday" :class="{ urgent: badge.urgent }">{{ badge.text }}</span>
    </div>
  </li>
</template>

<style scoped>
.row{display:grid;grid-template-columns:auto minmax(0,1fr) auto;align-items:start;gap:16px;padding:16px 22px;border-top:1px solid var(--line)}
.row.saving{opacity:.6;pointer-events:none}.main{min-width:0}.line,.fields,.actions{display:flex;align-items:center;gap:9px;flex-wrap:wrap}.name{margin:0;font-size:.94rem}.tag{padding:2px 7px;border-radius:999px;background:var(--surface-sunken);font-size:.7rem;color:var(--ink-muted)}
.status{padding:4px 8px;border:1px solid var(--line-strong);border-radius:999px;background:var(--surface);font-size:.74rem}.course{display:inline-block;margin-top:3px;font-size:.78rem;color:var(--ink-muted)}.desc{margin:5px 0 0;font-size:.83rem;white-space:pre-wrap;overflow-wrap:anywhere}.edit{padding:0;border:0;background:none;font-size:.74rem;color:var(--ink-muted);cursor:pointer}.edit:hover{color:var(--mint-deep)}.edit.danger:hover,.err{color:var(--danger)}
.editor{display:grid;gap:8px;margin-top:12px;padding:12px;background:var(--surface-sunken);border-radius:10px}.wide{width:100%}.fields .input{flex:1;min-width:140px}.actions{justify-content:flex-end}.btn-sm{padding:6px 14px;font-size:.8rem}.err{margin:7px 0 0;font-size:.78rem}.due{display:flex;flex-direction:column;align-items:flex-end;gap:2px;font-size:.8rem;color:var(--ink-soft);white-space:nowrap}.dday{font-size:.73rem;font-weight:600}.dday.urgent{color:var(--danger)}
@media(max-width:680px){.row{grid-template-columns:auto minmax(0,1fr)}.due{grid-column:2;align-items:flex-start}}
</style>
