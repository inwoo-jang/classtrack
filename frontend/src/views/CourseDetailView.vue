<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { assignmentApi, courseApi } from '@/api/courses'
import { ApiError } from '@/api/client'
import type { Assignment, AssignmentMode, AssignmentRequirement, Course } from '@/types/api'
import {
  COURSE_STATUS_LABEL,
  deliveryLabel,
  formatDate,
  isDone,
} from '@/utils/format'
import { subjectColor } from '@/utils/subjectColor'
import AssignmentRow from '@/components/AssignmentRow.vue'
import ProgressMeter from '@/components/ProgressMeter.vue'
import StateBlock from '@/components/StateBlock.vue'

const route = useRoute()
const courseId = Number(route.params.courseId)

const course = ref<Course | null>(null)
const assignments = ref<Assignment[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

const showForm = ref(false)
const submitting = ref(false)
const formError = ref<string | null>(null)
const form = reactive({
  title: '',
  description: '',
  dueDate: '',
  noDueDate: false,
  assignmentMode: 'INDIVIDUAL' as AssignmentMode,
  requirement: 'REQUIRED' as AssignmentRequirement,
  submissionUrlsText: '',
})

/** 서버 집계 대신 화면에서 직접 센다 — 상태를 바꿔도 재조회 없이 즉시 반영된다. */
const summary = computed(() => {
  const list = assignments.value
  const todo = list.filter((a) => a.status === 'TODO').length
  const inProgress = list.filter((a) => a.status === 'IN_PROGRESS').length
  const completed = list.filter((a) => a.status === 'COMPLETED').length
  const overdue = list.filter(
    (a) => a.dueDate && !isDone(a.status) && new Date(a.dueDate).getTime() < Date.now(),
  ).length

  return { total: list.length, todo, inProgress, completed, overdue }
})

const doneCount = computed(() => summary.value.completed)
const linkedCount = computed(() => assignments.value.filter((a) => a.submissionLinks.length).length)

function message(e: unknown): string {
  return e instanceof ApiError ? e.message : '알 수 없는 오류가 발생했습니다.'
}

function onUpdated(updated: Assignment) {
  const i = assignments.value.findIndex((a) => a.id === updated.id)
  if (i !== -1) assignments.value[i] = updated
}

function onRemoved(id: number) {
  assignments.value = assignments.value.filter((a) => a.id !== id)
}

onMounted(async () => {
  try {
    // 두 요청은 서로 의존하지 않으므로 동시에 보낸다.
    const [c, a] = await Promise.all([
      courseApi.detail(courseId),
      assignmentApi.list(courseId),
    ])
    course.value = c
    assignments.value = a
  } catch (e) {
    error.value = message(e)
  } finally {
    loading.value = false
  }
})

async function addAssignment() {
  submitting.value = true
  formError.value = null
  try {
    const created = await assignmentApi.create(courseId, {
      title: form.title,
      description: form.description || null,
      // datetime-local 은 "2026-03-20T23:59" 형태라 초를 붙여 LocalDateTime 에 맞춘다.
      dueDate: form.noDueDate
        ? null
        : form.dueDate.length === 16 ? `${form.dueDate}:00` : form.dueDate || null,
      assignmentMode: form.assignmentMode,
      requirement: form.requirement,
      submissionUrls: form.submissionUrlsText.split('\n').map((url) => url.trim()).filter(Boolean),
    })
    assignments.value.push(created)
    assignments.value.sort((a, b) => (a.dueDate ?? '9999').localeCompare(b.dueDate ?? '9999'))
    form.title = ''
    form.description = ''
    form.dueDate = ''
    form.noDueDate = false
    form.assignmentMode = 'INDIVIDUAL'
    form.requirement = 'REQUIRED'
    form.submissionUrlsText = ''
    showForm.value = false
  } catch (e) {
    formError.value = message(e)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <StateBlock :loading="loading" :error="error">
    <template v-if="course">
      <nav class="crumb">
        <RouterLink to="/courses">← 강의 목록</RouterLink>
      </nav>

      <header class="head">
        <div class="labels">
          <span class="subject">
            <span class="swatch" :style="{ background: subjectColor(course.subject) }" />
            {{ course.subject }}
          </span>
          <span class="phase" :class="`phase--${course.status.toLowerCase()}`">
            {{ COURSE_STATUS_LABEL[course.status] }}
          </span>
          <span class="mode">{{ deliveryLabel(course) }}</span>
        </div>

        <div class="title-row">
          <div class="title-main">
            <h1 class="page-title">{{ course.title }}</h1>
          </div>
          <RouterLink :to="`/courses/${courseId}/edit`" class="btn btn-ghost btn-edit">
            수정
          </RouterLink>
        </div>

        <dl class="facts">
          <div>
            <dt>강사</dt>
            <dd>{{ course.instructor }}</dd>
          </div>
          <div>
            <dt>기간</dt>
            <dd>
              {{ formatDate(course.startDate) }} — {{ formatDate(course.endDate) }}
              <span class="muted">({{ course.durationDays }}일)</span>
            </dd>
          </div>
          <div>
            <dt>장소</dt>
            <dd>{{ course.location }}</dd>
          </div>
          <div>
            <dt>실습교수</dt>
            <dd>
              {{ course.practiceProfessor ?? '—' }}
              <span v-if="!course.practiceProfessor && course.liveLecture" class="muted">
                (대면)
              </span>
            </dd>
          </div>
        </dl>
      </header>

      <section class="card panel">
        <header class="panel-head">
          <div class="panel-title">
            <h2 class="title">과제</h2>
            <p class="muted sub">
              {{ summary.total }}개 · 진행 전 {{ summary.todo }} · 진행 중
              {{ summary.inProgress }} · 완료 {{ doneCount }}
              <template v-if="summary.overdue">
                · <span class="alert">지연 {{ summary.overdue }}</span>
              </template>
              <template v-if="summary.total"> · 결과물 {{ linkedCount }}/{{ summary.total }}</template>
            </p>
          </div>
          <button class="btn btn-ghost" @click="showForm = !showForm">
            {{ showForm ? '취소' : '과제 추가' }}
          </button>
        </header>

        <div class="meter-wrap">
          <ProgressMeter :summary="summary" />
        </div>

        <form v-if="showForm" class="form" @submit.prevent="addAssignment">
          <p v-if="formError" class="notice notice-error span">{{ formError }}</p>

          <div class="field">
            <label for="a-title">과제명</label>
            <input
              id="a-title"
              v-model="form.title"
              class="input"
              required
              placeholder="예: 3주차 실습 과제"
            />
          </div>

          <div class="field">
            <label for="a-due">마감일</label>
            <input id="a-due" v-model="form.dueDate" class="input" type="datetime-local" :disabled="form.noDueDate" :required="!form.noDueDate" />
            <label class="check"><input v-model="form.noDueDate" type="checkbox" /> 마감 없음</label>
          </div>

          <div class="field">
            <label for="a-mode">과제 형태</label>
            <select id="a-mode" v-model="form.assignmentMode" class="input">
              <option value="INDIVIDUAL">개인</option><option value="TEAM">팀</option>
            </select>
          </div>

          <div class="field">
            <label for="a-requirement">필수 여부</label>
            <select id="a-requirement" v-model="form.requirement" class="input">
              <option value="REQUIRED">필수</option><option value="OPTIONAL">자율</option>
            </select>
          </div>

          <div class="field span">
            <label for="a-url">
              결과물 링크 <span class="muted">(선택 · GitHub / Google Drive)</span>
            </label>
            <textarea
              id="a-url"
              v-model="form.submissionUrlsText"
              class="input"
              rows="2"
              placeholder="링크를 한 줄에 하나씩 입력 (최대 10개)"
            />
          </div>

          <div class="field span">
            <label for="a-desc">설명 <span class="muted">(선택)</span></label>
            <textarea id="a-desc" v-model="form.description" class="input" rows="4" maxlength="5000" placeholder="줄바꿈 가능 · 최대 10줄" />
          </div>

          <div class="span form-actions">
            <button class="btn btn-primary" type="submit" :disabled="submitting">
              {{ submitting ? '추가 중…' : '추가' }}
            </button>
          </div>
        </form>

        <ul v-if="assignments.length" class="list">
          <AssignmentRow
            v-for="assignment in assignments"
            :key="assignment.id"
            :assignment="assignment"
            @updated="onUpdated"
            @removed="onRemoved"
          />
        </ul>
        <p v-else class="empty muted">등록된 과제가 없습니다.</p>
      </section>
    </template>
  </StateBlock>
</template>

<style scoped>
.crumb {
  padding: 32px 0 0;
  font-size: 0.85rem;
}

.crumb a {
  color: var(--ink-muted);
  transition: color 0.16s var(--ease);
}

.crumb a:hover {
  color: var(--mint-deep);
}

.head {
  padding: 16px 0 34px;
}

.labels {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.subject {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 9px;
  border: 1px solid var(--line-strong);
  border-radius: 5px;
  font-size: 0.7rem;
  font-weight: 500;
  color: var(--ink-soft);
}

.swatch {
  width: 7px;
  height: 7px;
  border-radius: 2px;
}

.mode {
  font-size: 0.7rem;
  letter-spacing: 0.04em;
  color: var(--ink-muted);
}

.title-main {
  min-width: 0;
}

.phase {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 0.7rem;
  color: var(--ink-muted);
}

.phase::before {
  content: '';
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: currentColor;
}

.phase--ongoing {
  color: var(--mint-deep);
}

.phase--ongoing::before {
  box-shadow: 0 0 0 3px var(--mint-wash);
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-title {
  font-size: 1.85rem;
  font-weight: 500;
  letter-spacing: -0.03em;
  line-height: 1.2;
}

.btn-edit {
  flex-shrink: 0;
  padding: 6px 15px;
  font-size: 0.83rem;
}

.facts {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  gap: 18px 26px;
  margin: 22px 0 0;
  padding-top: 22px;
  border-top: 1px solid var(--line);
}

.facts dt {
  font-size: 0.7rem;
  letter-spacing: 0.09em;
  text-transform: uppercase;
  color: var(--ink-muted);
}

.facts dd {
  margin: 4px 0 0;
  font-size: 0.92rem;
  color: var(--ink-soft);
}

.panel {
  overflow: hidden;
  margin-bottom: 72px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 22px 16px;
}

.panel-title {
  min-width: 0;
}

.sub {
  margin-top: 3px;
  font-size: 0.8rem;
}

.alert {
  color: var(--danger);
}

.meter-wrap {
  padding: 0 22px 16px;
}

.form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 15px;
  padding: 20px 22px;
  background: var(--surface-accent);
  border-top: 1px solid var(--line);
}

.form .span {
  grid-column: 1 / -1;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.form textarea.input {
  resize: vertical;
}

.list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.empty {
  padding: 44px 22px;
  text-align: center;
  border-top: 1px solid var(--line);
  font-size: 0.9rem;
}

@media (max-width: 620px) {
  .form {
    grid-template-columns: 1fr;
  }

  .panel-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
