<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { courseApi } from '@/api/courses'
import { ApiError } from '@/api/client'
import type { Course } from '@/types/api'
import CourseCard from '@/components/CourseCard.vue'
import StateBlock from '@/components/StateBlock.vue'

const courses = ref<Course[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

/** 강의 진행 상태 집계. 과제 지표는 과제 화면에서 본다. */
const totals = computed(() => ({
  total: courses.value.length,
  ongoing: courses.value.filter((c) => c.status === 'ONGOING').length,
  upcoming: courses.value.filter((c) => c.status === 'UPCOMING').length,
  finished: courses.value.filter((c) => c.status === 'FINISHED').length,
}))

onMounted(async () => {
  try {
    courses.value = await courseApi.list()
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
      <h1 class="page-title">강의</h1>
      <p class="muted page-sub">
        <template v-if="!loading && !error">{{ courses.length }}개 등록됨</template>
        <template v-else>&nbsp;</template>
      </p>
    </div>
    <RouterLink to="/courses/new" class="btn btn-primary">강의 등록</RouterLink>
  </header>

  <dl v-if="!loading && !error && courses.length" class="stats">
    <div class="stat accent">
      <dt>진행 중</dt>
      <dd>{{ totals.ongoing }}</dd>
    </div>
    <div class="stat">
      <dt>예정</dt>
      <dd>{{ totals.upcoming }}</dd>
    </div>
    <div class="stat">
      <dt>종료</dt>
      <dd>{{ totals.finished }}</dd>
    </div>
    <div class="stat">
      <dt>전체</dt>
      <dd>{{ totals.total }}</dd>
    </div>
  </dl>

  <StateBlock
    :loading="loading"
    :error="error"
    :empty="courses.length === 0"
    empty-text="아직 등록된 강의가 없습니다. 위에서 첫 강의를 추가해보세요."
  >
    <div class="grid">
      <CourseCard v-for="course in courses" :key="course.id" :course="course" />
    </div>
  </StateBlock>
</template>

<style scoped>
.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  padding: 44px 0 26px;
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

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 1px;
  margin: 0 0 30px;
  background: var(--line);
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  overflow: hidden;
}

.stat {
  background: var(--surface);
  padding: 15px 18px;
}

.stat dt {
  font-size: 0.72rem;
  letter-spacing: 0.07em;
  color: var(--ink-muted);
}

.stat dd {
  margin: 3px 0 0;
  font-size: 1.45rem;
  font-weight: 500;
  letter-spacing: -0.03em;
  font-variant-numeric: tabular-nums;
}

.stat.accent dd {
  color: var(--mint-deep);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(288px, 1fr));
  gap: 16px;
  margin-bottom: 72px;
}

@media (max-width: 520px) {
  .page-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }
}
</style>
