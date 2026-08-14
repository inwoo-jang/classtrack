<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { portfolioApi } from '@/api/courses'
import { ApiError } from '@/api/client'
import StateBlock from '@/components/StateBlock.vue'

const markdown = ref('')
const loading = ref(true)
const error = ref<string | null>(null)

/** 전체 기록 / 대표 과제만 */
const featuredOnly = ref(false)
const copied = ref(false)

const lineCount = computed(() => markdown.value.split('\n').length)
const charCount = computed(() => markdown.value.length)

async function load() {
  loading.value = true
  error.value = null
  try {
    markdown.value = await portfolioApi.markdown(featuredOnly.value)
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '생성에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

async function copy() {
  try {
    await navigator.clipboard.writeText(markdown.value)
    copied.value = true
    setTimeout(() => (copied.value = false), 1600)
  } catch {
    error.value = '클립보드 복사에 실패했습니다. 직접 선택해 복사해주세요.'
  }
}

/**
 * 서버가 파일로 내려주지 않고 본문만 준다. 프론트가 다른 출처(Vercel)에 있어
 * Content-Disposition 으로는 바로 저장되지 않기 때문이다. Blob 으로 만들어 저장한다.
 */
function download() {
  const blob = new Blob([markdown.value], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = featuredOnly.value ? 'portfolio-featured.md' : 'portfolio.md'
  a.click()
  URL.revokeObjectURL(url)
}

watch(featuredOnly, load)
onMounted(load)
</script>

<template>
  <header class="page-head">
    <div>
      <h1 class="page-title">포트폴리오</h1>
      <p class="muted page-sub">기록을 Markdown 으로 만들어 README·이력서에 붙여넣습니다.</p>
    </div>
  </header>

  <div class="bar">
    <div class="scope">
      <button class="chip" :class="{ on: !featuredOnly }" type="button" @click="featuredOnly = false">
        전체 기록
      </button>
      <button class="chip" :class="{ on: featuredOnly }" type="button" @click="featuredOnly = true">
        ★ 대표 과제만
      </button>
    </div>

    <div class="tools">
      <span v-if="!loading && !error" class="muted meta">{{ lineCount }}줄 · {{ charCount }}자</span>
      <button class="btn btn-ghost btn-sm" type="button" :disabled="loading" @click="download">
        .md 저장
      </button>
      <button class="btn btn-primary btn-sm" type="button" :disabled="loading" @click="copy">
        {{ copied ? '복사됨' : '복사' }}
      </button>
    </div>
  </div>

  <StateBlock :loading="loading" :error="error">
    <p v-if="featuredOnly && !markdown.includes('####')" class="notice hint">
      대표로 표시된 과제가 없습니다. 과제 편집에서 <b>대표 과제</b>를 체크하면 여기에 담깁니다.
    </p>

    <pre class="preview">{{ markdown }}</pre>
  </StateBlock>
</template>

<style scoped>
.page-head {
  padding: 44px 0 18px;
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

.bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.scope,
.tools {
  display: flex;
  align-items: center;
  gap: 7px;
}

.chip {
  padding: 6px 14px;
  border: 1px solid var(--line-strong);
  border-radius: 999px;
  background: transparent;
  font-size: 0.83rem;
  color: var(--ink-soft);
  cursor: pointer;
  transition:
    background 0.16s var(--ease),
    border-color 0.16s var(--ease),
    color 0.16s var(--ease);
}

.chip:hover {
  background: var(--surface);
  border-color: var(--ink-muted);
}

.chip.on {
  background: var(--mint-ink);
  border-color: var(--mint-ink);
  color: #fff;
}

.meta {
  font-size: 0.78rem;
  font-variant-numeric: tabular-nums;
}

.btn-sm {
  padding: 6px 14px;
  font-size: 0.82rem;
}

.hint {
  margin-bottom: 12px;
  font-size: 0.85rem;
}

/* 렌더링하지 않고 원문을 보여준다 — 어차피 붙여넣을 것이라 그대로가 정확하다 */
.preview {
  margin: 0 0 72px;
  padding: 18px 20px;
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  background: var(--surface);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12.5px;
  line-height: 1.7;
  color: var(--ink-soft);
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  max-height: 70vh;
  overflow-y: auto;
}
</style>
