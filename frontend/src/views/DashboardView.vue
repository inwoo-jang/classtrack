<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { dashboardApi } from '@/api/courses'
import { ApiError } from '@/api/client'
import type { Dashboard } from '@/types/api'
import { courseDayIndex, deadlineBadge, formatDateTime } from '@/utils/format'
import CourseCalendar from '@/components/CourseCalendar.vue'
import StateBlock from '@/components/StateBlock.vue'

const data = ref<Dashboard | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

const doneAssignments = computed(() => (data.value ? data.value.assignments.completed : 0))

/** 대시보드는 요약이므로 몇 개만 보여주고 나머지는 각 탭으로 보낸다. */
const LIMIT = 4

const ongoingShown = computed(() => data.value?.ongoingCourses.slice(0, LIMIT) ?? [])
const inProgressShown = computed(
  () => data.value?.inProgressAssignments.slice(0, LIMIT) ?? [],
)

onMounted(async () => {
  try {
    data.value = await dashboardApi.load()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '알 수 없는 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <header class="page-head">
    <h1 class="page-title">대시보드</h1>
    <RouterLink to="/courses/new" class="btn btn-primary">강의 등록</RouterLink>
  </header>

  <StateBlock :loading="loading" :error="error">
    <template v-if="data">
      <!-- 상단 집계 -->
      <div class="boards">
        <section class="card board">
          <header class="board-head">
            <h2 class="board-title">강의</h2>
            <RouterLink to="/courses" class="more">전체 보기 →</RouterLink>
          </header>
          <dl class="figures">
            <div class="figure accent">
              <dt>진행 중</dt>
              <dd>{{ data.courses.ongoing }}</dd>
            </div>
            <div class="figure">
              <dt>예정</dt>
              <dd>{{ data.courses.upcoming }}</dd>
            </div>
            <div class="figure">
              <dt>완료</dt>
              <dd>{{ data.courses.finished }}</dd>
            </div>
            <div class="figure">
              <dt>전체</dt>
              <dd>{{ data.courses.total }}</dd>
            </div>
          </dl>
        </section>

        <section class="card board">
          <header class="board-head">
            <h2 class="board-title">과제</h2>
            <RouterLink to="/assignments" class="more">전체 보기 →</RouterLink>
          </header>
          <dl class="figures">
            <div class="figure accent">
              <dt>진행 중</dt>
              <dd>{{ data.assignments.inProgress }}</dd>
            </div>
            <div class="figure">
              <dt>진행 전</dt>
              <dd>{{ data.assignments.todo }}</dd>
            </div>
            <div class="figure">
              <dt>완료</dt>
              <dd>{{ doneAssignments }}</dd>
            </div>
            <div class="figure" :class="{ alert: data.assignments.overdue > 0 }">
              <dt>마감 지남</dt>
              <dd>{{ data.assignments.overdue }}</dd>
            </div>
          </dl>
        </section>
      </div>

      <!-- 진행 중인 강의 · 진행 중인 과제 — 한 줄에 나란히 -->
      <div class="columns">
        <section class="col">
          <header class="col-head">
            <h2 class="col-title">진행 중인 강의</h2>
            <RouterLink to="/courses" class="more">{{ data.courses.ongoing }}개 →</RouterLink>
          </header>

          <ul v-if="ongoingShown.length" class="card list">
            <li v-for="course in ongoingShown" :key="course.id">
              <RouterLink :to="`/courses/${course.id}`" class="item">
                <div class="item-main">
                  <span class="tag">{{ course.subject }}</span>
                  <p class="item-title">{{ course.title }}</p>
                  <p class="item-sub muted">{{ course.instructor }} · {{ course.location }}</p>
                </div>
                <span class="day">
                  <b>{{ courseDayIndex(course) }}</b>/{{ course.durationDays }}
                </span>
              </RouterLink>
            </li>
          </ul>
          <p v-else class="card blank muted">오늘 진행 중인 강의가 없습니다.</p>
        </section>

        <section class="col">
          <header class="col-head">
            <h2 class="col-title">진행 중인 과제</h2>
            <RouterLink to="/assignments" class="more">
              {{ data.assignments.inProgress }}개 →
            </RouterLink>
          </header>

          <ul v-if="inProgressShown.length" class="card list">
            <li v-for="a in inProgressShown" :key="a.id">
              <RouterLink :to="`/courses/${a.courseId}`" class="item">
                <div class="item-main">
                  <span class="tag">{{ a.courseTitle }}</span>
                  <p class="item-title">{{ a.title }}</p>
                  <p class="item-sub muted">{{ formatDateTime(a.dueDate) }} 마감</p>
                </div>
                <span
                  v-if="deadlineBadge(a)"
                  class="dday"
                  :class="{ urgent: deadlineBadge(a)!.urgent }"
                >
                  {{ deadlineBadge(a)!.text }}
                </span>
              </RouterLink>
            </li>
          </ul>
          <p v-else class="card blank muted">진행 중인 과제가 없습니다.</p>
        </section>
      </div>

      <!-- 캘린더 -->
      <section class="section">
        <CourseCalendar />
      </section>
    </template>
  </StateBlock>
</template>

<style scoped>
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 44px 0 24px;
}

.page-title {
  font-size: 1.85rem;
  font-weight: 500;
  letter-spacing: -0.03em;
  line-height: 1.2;
}

/* ---- 상단 두 보드 ---- */

.boards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.board {
  padding: 18px 20px 20px;
}

.board-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.board-title {
  font-size: 0.95rem;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.more {
  font-size: 0.78rem;
  color: var(--ink-muted);
  white-space: nowrap;
  transition: color 0.16s var(--ease);
}

.more:hover {
  color: var(--ink);
}

.figures {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin: 0;
}

.figure dt {
  font-size: 0.7rem;
  color: var(--ink-muted);
  white-space: nowrap;
}

.figure dd {
  margin: 2px 0 0;
  font-size: 1.5rem;
  font-weight: 500;
  letter-spacing: -0.035em;
  font-variant-numeric: tabular-nums;
  line-height: 1.1;
}

.figure.accent dd {
  color: var(--mint-deep);
}

.figure.alert dd {
  color: var(--danger);
}

/* ---- 두 열 ---- */

.columns {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
  margin-top: 26px;
}

.col {
  min-width: 0;
}

.col-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.col-title {
  font-size: 0.95rem;
  font-weight: 500;
  letter-spacing: -0.015em;
}

.list {
  list-style: none;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

.list li + li {
  border-top: 1px solid var(--line);
}

.item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 11px 16px;
  transition: background 0.16s var(--ease);
}

.item:hover {
  background: var(--surface-sunken);
}

.item-main {
  min-width: 0;
}

.tag {
  display: inline-block;
  max-width: 100%;
  font-size: 0.68rem;
  color: var(--ink-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-title {
  margin: 1px 0 0;
  font-size: 0.88rem;
  font-weight: 500;
  letter-spacing: -0.012em;
  color: var(--ink);
  overflow-wrap: anywhere;
}

.item-sub {
  margin: 2px 0 0;
  font-size: 0.74rem;
  overflow-wrap: anywhere;
}

.day {
  flex-shrink: 0;
  font-size: 0.78rem;
  font-variant-numeric: tabular-nums;
  color: var(--ink-muted);
}

.day b {
  font-weight: 600;
  color: var(--ink);
}

.dday {
  flex-shrink: 0;
  font-size: 0.74rem;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  color: var(--ink-muted);
}

.dday.urgent {
  color: var(--danger);
}

.blank {
  padding: 26px 16px;
  text-align: center;
  font-size: 0.85rem;
}

/* ---- 캘린더 ---- */

.section {
  margin-top: 26px;
  margin-bottom: 72px;
}
</style>
