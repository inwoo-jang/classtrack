<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { calendarApi } from '@/api/courses'
import { ApiError } from '@/api/client'
import type { CalendarData, CalendarSession } from '@/types/api'
import { subjectColor } from '@/utils/subjectColor'

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']

const data = ref<CalendarData | null>(null)
const error = ref<string | null>(null)

/** 보고 있는 달의 1일. 달 이동은 이 값만 바꾼다. */
const cursor = ref(startOfMonth(new Date()))

function startOfMonth(d: Date): Date {
  return new Date(d.getFullYear(), d.getMonth(), 1)
}

/** Date -> "2026-07-14" (toISOString 은 UTC 로 밀리므로 쓰지 않는다) */
function iso(d: Date): string {
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

const monthLabel = computed(
  () => `${cursor.value.getFullYear()}년 ${cursor.value.getMonth() + 1}월`,
)

/** 달력 격자는 그 달을 포함하는 주 단위로 채운다 (앞뒤 달 며칠이 섞임). */
const gridDays = computed(() => {
  const first = cursor.value
  const start = new Date(first)
  start.setDate(1 - first.getDay())

  const days: Date[] = []
  for (let i = 0; i < 42; i++) {
    const d = new Date(start)
    d.setDate(start.getDate() + i)
    days.push(d)
  }
  // 마지막 주가 통째로 다음 달이면 잘라낸다
  return days.slice(0, days[35]!.getMonth() === first.getMonth() ? 42 : 35)
})

const holidaySet = computed(() => new Set(data.value?.holidays ?? []))

const sessionsByDate = computed(() => {
  const map = new Map<string, CalendarSession[]>()
  for (const s of data.value?.sessions ?? []) {
    const list = map.get(s.date) ?? []
    list.push(s)
    map.set(s.date, list)
  }
  return map
})

const todayIso = iso(new Date())

function isCurrentMonth(d: Date) {
  return d.getMonth() === cursor.value.getMonth()
}

function shiftMonth(delta: number) {
  const next = new Date(cursor.value)
  next.setMonth(next.getMonth() + delta)
  cursor.value = startOfMonth(next)
}

async function load() {
  error.value = null
  try {
    const days = gridDays.value
    data.value = await calendarApi.range(iso(days[0]!), iso(days[days.length - 1]!))
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '캘린더를 불러오지 못했습니다.'
  }
}

// 격자에 보이는 범위만 요청한다. 달을 옮기면 다시 받는다.
watch(cursor, load, { immediate: true })
</script>

<template>
  <section class="wrap">
    <header class="head">
      <h2 class="title">캘린더</h2>
      <div class="nav">
        <button class="nav-btn" type="button" @click="shiftMonth(-1)" aria-label="이전 달">
          ←
        </button>
        <span class="month">{{ monthLabel }}</span>
        <button class="nav-btn" type="button" @click="shiftMonth(1)" aria-label="다음 달">→</button>
        <button class="nav-btn" type="button" @click="cursor = startOfMonth(new Date())">
          오늘
        </button>
      </div>
    </header>

    <p v-if="error" class="err">{{ error }}</p>

    <div class="card grid-wrap">
      <div class="weekdays">
        <span v-for="(w, i) in WEEKDAYS" :key="w" :class="{ sun: i === 0, sat: i === 6 }">
          {{ w }}
        </span>
      </div>

      <div class="grid">
        <div
          v-for="d in gridDays"
          :key="iso(d)"
          class="cell"
          :class="{
            out: !isCurrentMonth(d),
            today: iso(d) === todayIso,
            holiday: holidaySet.has(iso(d)),
            weekend: d.getDay() === 0 || d.getDay() === 6,
          }"
        >
          <span class="daynum">{{ d.getDate() }}</span>

          <div class="chips">
            <RouterLink
              v-for="s in sessionsByDate.get(iso(d)) ?? []"
              :key="`${s.courseId}-${s.date}`"
              :to="`/courses/${s.courseId}`"
              class="chip"
              :style="{ '--c': subjectColor(s.subject) }"
              :title="`${s.subject} · ${s.courseTitle} (${s.dayIndex}/${s.totalDays}일차)`"
            >
              <span class="chip-title">{{ s.courseTitle }}</span>
              <span v-if="s.totalDays > 1" class="chip-day">
                {{ s.dayIndex }}/{{ s.totalDays }}
              </span>
            </RouterLink>
          </div>
        </div>
      </div>
    </div>

    <p class="legend muted">
      <span class="key weekend-key" />주말
      <span class="key holiday-key" />공휴일
      <span class="key session-key" />수업일 (색은 과목별)
    </p>
  </section>
</template>

<style scoped>
.head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.title {
  font-size: 1.02rem;
  font-weight: 500;
  letter-spacing: -0.02em;
}

.nav {
  display: flex;
  align-items: center;
  gap: 5px;
}

.nav-btn {
  padding: 4px 10px;
  border: 1px solid var(--line-strong);
  border-radius: var(--r-sm);
  background: var(--surface);
  font-size: 0.8rem;
  color: var(--ink-soft);
  cursor: pointer;
  transition:
    border-color 0.16s var(--ease),
    color 0.16s var(--ease);
}

.nav-btn:hover {
  border-color: var(--ink-muted);
  color: var(--ink);
}

.month {
  min-width: 104px;
  text-align: center;
  font-size: 0.88rem;
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}

.err {
  margin: 0 0 10px;
  font-size: 0.83rem;
  color: var(--danger);
}

/* ── 격자 ── */

.grid-wrap {
  overflow: hidden;
}

.weekdays {
  display: grid;
  /* minmax(0, 1fr) — 1fr 만 쓰면 내용이 넓을 때 칸이 밀려난다 */
  grid-template-columns: repeat(7, minmax(0, 1fr));
  border-bottom: 1px solid var(--line);
}

.weekdays span {
  padding: 8px 0;
  text-align: center;
  font-size: 0.7rem;
  letter-spacing: 0.05em;
  color: var(--ink-muted);
}

.weekdays .sun {
  color: var(--danger);
}

.grid {
  display: grid;
  /* 7칸을 항상 같은 너비로. 내용 길이가 칸 크기를 바꾸지 못하게 한다. */
  grid-template-columns: repeat(7, minmax(0, 1fr));
  grid-auto-rows: minmax(94px, auto);
}

.cell {
  /* grid/flex 자식의 기본 min-width 는 auto 라서, 0 으로 낮춰야 내용이 줄어든다 */
  min-width: 0;
  overflow: hidden;
  padding: 5px 6px 7px;
  border-right: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.cell:nth-child(7n) {
  border-right: 0;
}

.cell.weekend {
  background: var(--surface-sunken);
}

.cell.holiday {
  background: var(--danger-wash);
}

.cell.out {
  opacity: 0.38;
}

.daynum {
  font-size: 0.72rem;
  font-variant-numeric: tabular-nums;
  color: var(--ink-muted);
}

.cell.today .daynum {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 19px;
  height: 19px;
  margin: -2px 0 -1px -2px;
  border-radius: 50%;
  background: var(--ink);
  color: #fff;
  font-weight: 600;
}

.chips {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

/* 라벨 전체를 과목 색으로 칠한다. 선 하나로는 구분이 잘 안 된다.
   color-mix 로 같은 색에서 배경(연하게)과 글자(진하게)를 만든다. */
.chip {
  display: block;
  min-width: 0;
  padding: 2px 6px;
  border-radius: 4px;
  background: color-mix(in srgb, var(--c) 16%, #fff);
  border: 1px solid color-mix(in srgb, var(--c) 30%, #fff);
  font-size: 0.68rem;
  line-height: 1.32;
  color: color-mix(in srgb, var(--c) 72%, #1a1d2b);
  transition: background 0.16s var(--ease);
}

.chip:hover {
  background: color-mix(in srgb, var(--c) 30%, #fff);
}

/* 두 줄까지 보여주고 그 이상은 자른다 */
.chip-title {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  overflow-wrap: anywhere;
}

.chip-day {
  display: inline-block;
  margin-top: 1px;
  font-variant-numeric: tabular-nums;
  opacity: 0.7;
}

/* ── 범례 ── */

.legend {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 10px 0 0;
  font-size: 0.74rem;
}

.key {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 3px;
  border: 1px solid var(--line);
  margin-left: 10px;
}

.key:first-child {
  margin-left: 0;
}

.weekend-key {
  background: var(--surface-sunken);
}

.holiday-key {
  background: var(--danger-wash);
}

.session-key {
  background: color-mix(in srgb, #7b7fd4 16%, #fff);
  border-color: color-mix(in srgb, #7b7fd4 30%, #fff);
}

@media (max-width: 720px) {
  .cell {
    min-height: 64px;
    padding: 4px;
  }

  .chip-day {
    display: none;
  }
}
</style>
