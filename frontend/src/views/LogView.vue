<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { logApi } from '@/api/courses'
import { ApiError } from '@/api/client'
import type { LogEntry } from '@/types/api'
import DevTabs from '@/components/DevTabs.vue'

const LEVELS = ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR'] as const
const POLL_MS = 2000

const entries = ref<LogEntry[]>([])
const error = ref<string | null>(null)
const loading = ref(true)

const disabled = ref(false)
const level = ref<string>('DEBUG')
const keyword = ref('')
const following = ref(true)

/** 마지막으로 받은 sequence. 폴링 시 이보다 새 것만 요청한다. */
let lastSequence = 0
let timer: number | undefined

async function loadAll() {
  loading.value = true
  error.value = null
  try {
    const fetched = await logApi.list({ level: level.value, q: keyword.value, limit: 500 })
    entries.value = fetched
    lastSequence = fetched.length ? Math.max(...fetched.map((e) => e.sequence)) : 0
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) {
      disabled.value = true
      following.value = false
    } else {
      error.value = e instanceof ApiError ? e.message : '로그를 불러오지 못했습니다.'
    }
  } finally {
    loading.value = false
  }
}

/** 새 로그만 받아 앞에 붙인다 (목록은 최신이 위). */
async function poll() {
  try {
    const fresh = await logApi.list({
      after: lastSequence,
      level: level.value,
      q: keyword.value,
      limit: 200,
    })
    if (fresh.length) {
      lastSequence = Math.max(...fresh.map((e) => e.sequence))
      entries.value = [...fresh, ...entries.value].slice(0, 500)
    }
    error.value = null
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '폴링 실패'
  }
}

function startPolling() {
  stopPolling()
  timer = window.setInterval(poll, POLL_MS)
}

function stopPolling() {
  if (timer !== undefined) {
    window.clearInterval(timer)
    timer = undefined
  }
}

async function clearLogs() {
  await logApi.clear()
  entries.value = []
  lastSequence = 0
}

// 필터가 바뀌면 조건이 달라지므로 처음부터 다시 받는다
watch([level, keyword], loadAll)

watch(following, (on) => (on ? startPolling() : stopPolling()))

onMounted(async () => {
  await loadAll()
  if (following.value) startPolling()
})

// 화면을 떠나면 반드시 정리한다. 안 하면 요청이 계속 나간다.
onUnmounted(stopPolling)
</script>

<template>
  <DevTabs />

  <p v-if="disabled" class="notice off">
    이 환경에서는 로그 보드가 꺼져 있습니다. 스택트레이스와 요청 인자가 담기므로 기본값이
    <b>꺼짐</b>입니다. 켜려면 서버 환경변수에 <code>DEV_TOOLS_ENABLED=true</code> 를 넣으세요.
  </p>

  <header v-else class="head">
    <div class="controls">
      <select v-model="level" class="ctl" aria-label="최소 레벨">
        <option v-for="l in LEVELS" :key="l" :value="l">{{ l }}</option>
      </select>
      <input v-model="keyword" class="ctl search" type="search" placeholder="검색" />
      <label class="ctl follow">
        <input v-model="following" type="checkbox" />
        실시간
      </label>
      <button class="ctl btn-clear" type="button" @click="clearLogs">비우기</button>
    </div>
  </header>

  <p v-if="error" class="err">{{ error }}</p>

  <div v-if="!disabled" class="console">
    <p v-if="loading" class="note">불러오는 중…</p>
    <p v-else-if="!entries.length" class="note">로그 없음</p>
    <pre
      v-for="entry in entries"
      :key="entry.sequence"
      class="line"
      :class="`lv-${entry.level.toLowerCase()}`"
    >{{ entry.raw }}</pre>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  flex-wrap: wrap;
  padding: 0 0 14px;
}

.controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.ctl {
  padding: 6px 11px;
  border: 1px solid var(--line-strong);
  border-radius: var(--r-sm);
  background: var(--surface);
  font-size: 0.83rem;
}

.search {
  width: 180px;
}

.follow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  user-select: none;
}

.follow input {
  accent-color: var(--mint-deep);
  margin: 0;
}

.btn-clear {
  cursor: pointer;
  color: var(--ink-soft);
}

.btn-clear:hover {
  border-color: var(--danger);
  color: var(--danger);
}

.off {
  margin-bottom: 16px;
  font-size: 0.85rem;
  line-height: 1.6;
}

.off code {
  padding: 1px 5px;
  border-radius: 4px;
  background: var(--surface);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 0.78rem;
}

.err {
  margin: 0 0 10px;
  font-size: 0.83rem;
  color: var(--danger);
}

/* 콘솔 원문 그대로. 가공하지 않는다. */
.console {
  border: 1px solid var(--line);
  border-radius: var(--r-sm);
  background: var(--surface);
  padding: 10px 0;
  margin-bottom: 60px;
  overflow-x: auto;
}

.line {
  margin: 0;
  padding: 1px 16px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  line-height: 1.55;
  white-space: pre;
  color: var(--ink-soft);
}

.line:hover {
  background: var(--surface-sunken);
}

.lv-warn {
  color: var(--amber-ink);
}

.lv-error {
  color: var(--danger);
}

.lv-debug,
.lv-trace {
  color: var(--ink-muted);
}

.note {
  margin: 0;
  padding: 24px 16px;
  font-size: 0.85rem;
  color: var(--ink-muted);
}
</style>
