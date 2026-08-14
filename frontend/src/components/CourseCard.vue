<script setup lang="ts">
import { computed } from 'vue'
import type { Course } from '@/types/api'
import { COURSE_STATUS_LABEL, deliveryLabel, formatDate, roomOf } from '@/utils/format'
import { subjectColor } from '@/utils/subjectColor'
import ProgressMeter from './ProgressMeter.vue'

const props = defineProps<{ course: Course }>()

const summary = computed(() => props.course.assignments)
const done = computed(() => summary.value.completed)

const color = computed(() => subjectColor(props.course.subject))

/** 카드에는 앞의 몇 개만. 전체는 상세 화면에서 본다. */
const TECH_LIMIT = 5
const shownTech = computed(() => props.course.technologies.slice(0, TECH_LIMIT))
const restTech = computed(() => props.course.technologies.length - shownTech.value.length)

/** 하루짜리면 날짜 하나만. */
const period = computed(() => {
  const c = props.course
  return c.startDate === c.endDate
    ? formatDate(c.startDate)
    : `${formatDate(c.startDate)} – ${formatDate(c.endDate)}`
})

/** 강사 / 실습교수를 한 줄로. 실습교수가 없으면 강사만. */
const people = computed(() => {
  const c = props.course
  return c.practiceProfessor ? `${c.instructor} · 실습 ${c.practiceProfessor}` : c.instructor
})
</script>

<template>
  <RouterLink :to="`/courses/${course.id}`" class="card course">
    <header class="head">
      <span class="chip subject" :style="{ '--c': color }">
        <span class="swatch" />
        {{ course.subject }}
      </span>
      <span class="chip">{{ deliveryLabel(course) }}</span>

      <span class="spacer" />

      <span class="phase" :class="`phase--${course.status.toLowerCase()}`">
        {{ COURSE_STATUS_LABEL[course.status] }}
      </span>
    </header>

    <div class="title-row">
      <h3 class="name">{{ course.title }}</h3>
      <span class="days" :style="{ '--c': color }">{{ course.durationDays }} Days</span>
    </div>

    <p v-if="course.technologies.length" class="techs">
      <span v-for="tech in shownTech" :key="tech" class="tech">{{ tech }}</span>
      <span v-if="restTech > 0" class="tech more">+{{ restTech }}</span>
    </p>

    <p class="people">{{ people }}</p>
    <p class="when">{{ period }} · {{ roomOf(course.location) }}</p>

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
  padding: 18px 20px 20px;
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

/* ── 라벨 줄 ── */

.head {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  min-width: 0;
}

.spacer {
  flex: 1;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  flex-shrink: 0;
  padding: 2px 8px;
  border: 1px solid var(--line-strong);
  border-radius: 5px;
  font-size: 0.68rem;
  font-weight: 500;
  color: var(--ink-soft);
  white-space: nowrap;
}

/* 과목 칩만 색을 띤다 */
.chip.subject {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  border-color: color-mix(in srgb, var(--c) 34%, #fff);
  background: color-mix(in srgb, var(--c) 10%, #fff);
  color: color-mix(in srgb, var(--c) 72%, #1a1d2b);
}

.swatch {
  flex-shrink: 0;
  width: 6px;
  height: 6px;
  border-radius: 2px;
  background: var(--c);
}

.phase {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  flex-shrink: 0;
  font-size: 0.68rem;
  letter-spacing: 0.05em;
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

/* ── 강의명 + 기간 배지 ── */

.title-row {
  display: flex;
  align-items: baseline;
  gap: 9px;
  min-width: 0;
}

.name {
  flex: 1;
  min-width: 0;
  margin: 0;
  font-size: 1.1rem;
  font-weight: 500;
  letter-spacing: -0.026em;
  line-height: 1.32;
  color: var(--ink);
  overflow-wrap: anywhere;
}

.days {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 5px;
  background: color-mix(in srgb, var(--c) 16%, #fff);
  color: color-mix(in srgb, var(--c) 74%, #1a1d2b);
  font-size: 0.7rem;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.01em;
  white-space: nowrap;
}

/* ── 기술 칩 ── */

.techs {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin: 11px 0 0;
}

.tech {
  padding: 2px 8px;
  border: 1px solid var(--line);
  border-radius: 5px;
  background: var(--surface-sunken);
  font-size: 0.7rem;
  color: var(--ink-soft);
  white-space: nowrap;
}

.tech.more {
  background: transparent;
  color: var(--ink-muted);
}

/* ── 정보 두 줄 ── */

.people {
  margin: 9px 0 0;
  font-size: 0.82rem;
  color: var(--ink-soft);
  overflow-wrap: anywhere;
}

.when {
  margin: 2px 0 0;
  font-size: 0.76rem;
  color: var(--ink-muted);
  overflow-wrap: anywhere;
}

/* ── 과제 ── */

.progress {
  display: flex;
  flex-direction: column;
  gap: 9px;
  margin-top: 15px;
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
