<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { courseApi, subjectApi } from '@/api/courses'
import { ApiError } from '@/api/client'
import StateBlock from '@/components/StateBlock.vue'
import SubjectSelect from '@/components/SubjectSelect.vue'

const router = useRouter()
const route = useRoute()

/** 경로에 courseId 가 있으면 수정, 없으면 등록. 폼은 하나로 쓴다. */
const courseId = computed(() =>
  route.params.courseId ? Number(route.params.courseId) : null,
)
const isEdit = computed(() => courseId.value !== null)

const form = reactive({
  title: '',
  subject: '',
  instructor: '',
  startDate: '',
  durationDays: 1,
  location: '',
  liveLecture: true,
  practiceProfessor: '',
})

const subjects = ref<string[]>([])
const loading = ref(false)
const loadError = ref<string | null>(null)
const submitting = ref(false)
const deleting = ref(false)
const error = ref<string | null>(null)

function message(e: unknown): string {
  return e instanceof ApiError ? e.message : '알 수 없는 오류가 발생했습니다.'
}

onMounted(async () => {
  // 과목 목록은 등록·수정 양쪽에서 필요하다. 실패해도 직접 입력할 수 있으므로 무시한다.
  subjectApi.list().then((list) => (subjects.value = list)).catch(() => {})

  if (!isEdit.value) return

  loading.value = true
  try {
    const course = await courseApi.detail(courseId.value!)
    Object.assign(form, {
      title: course.title,
      subject: course.subject,
      instructor: course.instructor,
      startDate: course.startDate,
      durationDays: course.durationDays,
      location: course.location,
      liveLecture: course.liveLecture,
      practiceProfessor: course.practiceProfessor ?? '',
    })
  } catch (e) {
    loadError.value = message(e)
  } finally {
    loading.value = false
  }
})

async function remove() {
  if (!window.confirm('이 강의를 삭제할까요?\n딸린 과제도 함께 삭제되며 되돌릴 수 없습니다.')) {
    return
  }

  deleting.value = true
  error.value = null
  try {
    await courseApi.remove(courseId.value!)
    router.push('/courses')
  } catch (e) {
    error.value = message(e)
    deleting.value = false
  }
}

/**
 * 서버의 @AssertTrue 와 같은 규칙을 화면에서도 먼저 검사한다.
 * (서버 검증이 최종 관문이고, 이건 사용자 편의를 위한 것)
 */
const needsPracticeProfessor = computed(() => !form.liveLecture)
const practiceProfessorMissing = computed(
  () => needsPracticeProfessor.value && form.practiceProfessor.trim() === '',
)

async function submit() {
  if (practiceProfessorMissing.value) return

  submitting.value = true
  error.value = null

  const body = {
    title: form.title,
    subject: form.subject,
    instructor: form.instructor,
    startDate: form.startDate,
    durationDays: Number(form.durationDays),
    location: form.location,
    liveLecture: form.liveLecture,
    practiceProfessor: form.practiceProfessor.trim() || null,
  }

  try {
    const saved = isEdit.value
      ? await courseApi.update(courseId.value!, body)
      : await courseApi.create(body)

    router.push(`/courses/${saved.id}`)
  } catch (e) {
    error.value = message(e)
    submitting.value = false
  }
}
</script>

<template>
  <nav class="crumb">
    <RouterLink :to="isEdit ? `/courses/${courseId}` : '/courses'">
      {{ isEdit ? '← 강의 상세' : '← 강의 목록' }}
    </RouterLink>
  </nav>

  <header class="head">
    <h1 class="page-title">{{ isEdit ? '강의 수정' : '강의 등록' }}</h1>
    <p class="muted page-sub">
      {{ isEdit ? '강의 번호(PK)를 제외한 모든 항목을 바꿀 수 있습니다.'
               : '등록하면 바로 과제를 추가할 수 있습니다.' }}
    </p>
  </header>

  <StateBlock :loading="loading" :error="loadError">
  <form class="card form" @submit.prevent="submit">
    <p v-if="error" class="notice notice-error span">{{ error }}</p>

    <div class="field span">
      <label for="title">강의명</label>
      <input id="title" v-model="form.title" class="input" required placeholder="예: Spring Boot 백엔드 심화" />
    </div>

    <div class="field">
      <label for="subject">과목</label>
      <SubjectSelect id="subject" v-model="form.subject" :options="subjects" />
    </div>

    <div class="field">
      <label for="instructor">강사명</label>
      <input id="instructor" v-model="form.instructor" class="input" required />
    </div>

    <div class="field">
      <label for="startDate">시작일</label>
      <input id="startDate" v-model="form.startDate" class="input" type="date" required />
    </div>

    <div class="field">
      <label for="durationDays">수강 기간 (일)</label>
      <input
        id="durationDays"
        v-model.number="form.durationDays"
        class="input"
        type="number"
        min="1"
        required
      />
    </div>

    <div class="field span">
      <label for="location">강의 장소</label>
      <input id="location" v-model="form.location" class="input" required placeholder="예: 강남 캠퍼스 3층" />
    </div>

    <div class="field span">
      <span class="legend">진행 방식</span>
      <div class="choices">
        <label class="choice" :class="{ on: form.liveLecture }">
          <input v-model="form.liveLecture" type="radio" :value="true" />
          <span>대면</span>
        </label>
        <label class="choice" :class="{ on: !form.liveLecture }">
          <input v-model="form.liveLecture" type="radio" :value="false" />
          <span>비대면</span>
        </label>
      </div>
    </div>

    <div class="field span">
      <label for="practiceProfessor">
        실습교수
        <span class="muted">{{ needsPracticeProfessor ? '(필수)' : '(선택)' }}</span>
      </label>
      <input
        id="practiceProfessor"
        v-model="form.practiceProfessor"
        class="input"
        :required="needsPracticeProfessor"
      />
      <span v-if="practiceProfessorMissing" class="field-error">
        비대면 강의는 실습교수 이름이 필수입니다.
      </span>
    </div>

    <div class="span actions">
      <button
        v-if="isEdit"
        class="btn btn-danger"
        type="button"
        :disabled="deleting || submitting"
        @click="remove"
      >
        {{ deleting ? '삭제 중…' : '삭제' }}
      </button>

      <span class="spacer" />

      <RouterLink :to="isEdit ? `/courses/${courseId}` : '/courses'" class="btn btn-ghost">
        취소
      </RouterLink>
      <button class="btn btn-primary" type="submit" :disabled="submitting || deleting">
        {{ submitting ? '저장 중…' : isEdit ? '저장' : '등록' }}
      </button>
    </div>
  </form>
  </StateBlock>
</template>

<style scoped>
.crumb {
  padding: 32px 0 0;
  font-size: 0.85rem;
}

.page-title {
  font-size: 1.85rem;
  font-weight: 500;
  letter-spacing: -0.03em;
  line-height: 1.2;
}

.page-sub {
  margin-top: 4px;
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
  padding: 16px 0 30px;
}

.form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  padding: 30px;
  margin-bottom: 72px;
}

.span {
  grid-column: 1 / -1;
}

.legend {
  font-size: 0.82rem;
  font-weight: 500;
  color: var(--ink-soft);
}

.choices {
  display: flex;
  gap: 10px;
}

.choice {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: 1px solid var(--line-strong);
  border-radius: 999px;
  font-size: 0.88rem;
  color: var(--ink-soft);
  cursor: pointer;
  transition:
    border-color 0.18s var(--ease),
    background 0.18s var(--ease),
    color 0.18s var(--ease);
}

.choice input {
  accent-color: var(--mint-deep);
  margin: 0;
}

.choice.on {
  border-color: var(--mint);
  background: var(--mint-wash);
  color: var(--mint-ink);
}

.spacer {
  flex: 1;
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-top: 6px;
  border-top: 1px solid var(--line);
  margin-top: 4px;
}

.actions .btn-ghost {
  align-self: center;
  margin-top: 16px;
}

.actions .btn-primary {
  margin-top: 16px;
}

@media (max-width: 620px) {
  .form {
    grid-template-columns: 1fr;
    padding: 24px 20px;
  }
}
</style>
