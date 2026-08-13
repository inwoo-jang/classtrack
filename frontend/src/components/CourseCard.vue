<script setup lang="ts">
import { computed } from 'vue'
import type { Course } from '@/types/api'
import { COURSE_STATUS_LABEL, deliveryLabel, formatDate } from '@/utils/format'
import { subjectColor } from '@/utils/subjectColor'
import ProgressMeter from './ProgressMeter.vue'

const props = defineProps<{ course: Course }>()

const summary = computed(() => props.course.assignments)
const done = computed(() => summary.value.completed)

/** 강사 · 기간 · 장소를 한 줄로. 값이 없는 항목은 빼고 가운뎃점으로 잇는다. */
const metaLine = computed(() => {
  const c = props.course
  const period =
    c.startDate === c.endDate
      ? formatDate(c.startDate)
      : `${formatDate(c.startDate)} – ${formatDate(c.endDate)}`

  return [
    c.instructor,
    c.practiceProfessor ? `실습 ${c.practiceProfessor}` : null,
    `${period} · ${c.durationDays}일`,
    c.location,
  ]
    .filter(Boolean)
    .join(' · ')
})
</script>

<template>
  <RouterLink :to="`/courses/${course.id}`" class="card course">
    <header class="head">
      <span class="subject">
        <span class="swatch" :style="{ background: subjectColor(course.subject) }" />
        {{ course.subject }}
      </span>
      <span class="phase" :class="`phase--${course.status.toLowerCase()}`">
        {{ COURSE_STATUS_LABEL[course.status] }}
      </span>
    </header>

    <h3 class="name">{{ course.title }}</h3>
    <p class="meta">{{ deliveryLabel(course) }} · {{ metaLine }}</p>

    <div class="progress">
      <ProgressMeter :summary="summary" />

      <div v-if="summary.total > 0" class="counts">
        <span class="count"><b>{{ summary.total }}</b> 과제</span>
        <span v-if="summary.todo" class="count dot dot--todo">진행 전 {{ summary.todo }}</span>
        <span v-if="summary.inProgress" class="count dot dot--progress">
          진행 중 {{ summary.inProgress }}
        </span>
        <span v-if="done" class="count dot dot--done">완료 {{ done }}</span>
        <span v-if="summary.overdue" class="count overdue">지연 {{ summary.overdue }}</span>
      </div>
      <p v-else class="counts muted">등록된 과제 없음</p>
    </div>
  </RouterLink>
</template>

<style scoped>
.course {
  display: flex;
  flex-direction: column;
  padding: 20px 22px;
  transition:
    border-color 0.2s var(--ease),
    box-shadow 0.2s var(--ease),
    transform 0.2s var(--ease);
}

.course:hover {
  border-color: var(--ink-muted);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 11px;
  min-width: 0;
}

.swatch {
  flex-shrink: 0;
  width: 7px;
  height: 7px;
  border-radius: 2px;
}

.phase {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  font-size: 0.68rem;
  letter-spacing: 0.06em;
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

.phase--finished {
  opacity: 0.65;
}

.mode {
  font-size: 0.68rem;
  letter-spacing: 0.04em;
  color: var(--ink-muted);
}

/* 과목명 — 분류 라벨. 테두리만 있는 얇은 칩으로 조용하게 둔다. */
.subject {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 100%;
  padding: 2px 9px;
  border: 1px solid var(--line-strong);
  border-radius: 5px;
  font-size: 0.7rem;
  font-weight: 500;
  letter-spacing: 0.01em;
  color: var(--ink-soft);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 강의명 — 카드의 얼굴 */
.name {
  margin: 0;
  font-size: 1.12rem;
  font-weight: 500;
  letter-spacing: -0.026em;
  line-height: 1.32;
  color: var(--ink);
  overflow-wrap: anywhere;
}

.meta {
  margin: 3px 0 0;
  font-size: 0.76rem;
  line-height: 1.5;
  color: var(--ink-muted);
  overflow-wrap: anywhere;
}

.progress {
  display: flex;
  flex-direction: column;
  gap: 9px;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid var(--line);
}

.counts {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 5px 12px;
  margin: 0;
  font-size: 0.755rem;
  color: var(--ink-soft);
}

.count b {
  font-weight: 600;
  color: var(--ink);
}

.dot {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.dot::before {
  content: '';
  width: 5px;
  height: 5px;
  border-radius: 50%;
}

.dot--todo::before {
  background: var(--line-strong);
}

.dot--progress::before {
  background: var(--mint);
}

.dot--done::before {
  background: var(--ink);
}

.overdue {
  color: var(--danger);
  font-weight: 500;
}
</style>
