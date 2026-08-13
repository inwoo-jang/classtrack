<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { devApi } from '@/api/courses'
import { ApiError } from '@/api/client'
import type { DevOverview } from '@/types/api'
import DevTabs from '@/components/DevTabs.vue'
import StateBlock from '@/components/StateBlock.vue'

const data = ref<DevOverview | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

/** 서비스 클래스별로 묶어서 보여준다. */
const servicesByClass = computed(() => {
  const map = new Map<string, DevOverview['serviceMethods']>()
  for (const m of data.value?.serviceMethods ?? []) {
    const list = map.get(m.serviceClass) ?? []
    list.push(m)
    map.set(m.serviceClass, list)
  }
  return [...map.entries()]
})

/** 리소스 묶음별로 나눈다. 서버가 이미 순서대로 보내므로 순서를 유지한다. */
const endpointGroups = computed(() => {
  const map = new Map<string, DevOverview['endpoints']>()
  for (const e of data.value?.endpoints ?? []) {
    const list = map.get(e.group) ?? []
    list.push(e)
    map.set(e.group, list)
  }
  return [...map.entries()]
})

const endpointCount = computed(() => data.value?.endpoints.length ?? 0)
const documented = computed(
  () => data.value?.endpoints.filter((e) => e.description).length ?? 0,
)

onMounted(async () => {
  try {
    data.value = await devApi.overview()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '구현 현황을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <DevTabs />

  <p class="lead-note muted">
    아래 내용은 실행 중인 애플리케이션에서 직접 읽어옵니다 — 핸들러 매핑, Bean 애노테이션,
    JPA Metamodel. 손으로 관리하는 표가 아니므로 코드를 고치면 자동으로 반영됩니다.
  </p>

  <StateBlock :loading="loading" :error="error">
    <template v-if="data">
      <!-- ── API ── -->
      <section class="section">
        <header class="sec-head">
          <h2 class="sec-title">API</h2>
          <span class="muted n">{{ endpointCount }}개 · 설명 {{ documented }}/{{ endpointCount }}</span>
        </header>

        <div class="card table-wrap">
          <table class="tbl">
            <thead>
              <tr>
                <th class="w-method">메서드</th>
                <th>경로</th>
                <th>기능</th>
                <th>핸들러</th>
                <th>AOP</th>
              </tr>
            </thead>
            <tbody>
              <template v-for="[group, list] in endpointGroups" :key="group">
                <tr class="group-row">
                  <td colspan="5">/{{ group }}</td>
                </tr>
                <tr v-for="e in list" :key="`${e.httpMethod} ${e.path}`">
                <td>
                  <span class="verb" :class="`verb--${e.httpMethod.toLowerCase()}`">
                    {{ e.httpMethod }}
                  </span>
                </td>
                <td class="mono col-path">{{ e.path }}</td>
                <td class="col-desc">{{ e.description ?? '—' }}</td>
                <td class="mono dim col-handler">{{ e.controller }}.{{ e.handler }}</td>
                  <td>
                    <span v-for="a in e.annotations" :key="a" class="tag">{{ a }}</span>
                    <span v-if="!e.annotations.length" class="dim">—</span>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </section>

      <!-- ── 서비스 계층 ── -->
      <section class="section">
        <header class="sec-head">
          <h2 class="sec-title">서비스 계층</h2>
          <span class="muted n">트랜잭션 · AOP 로깅 적용 여부</span>
        </header>

        <div class="card table-wrap">
          <table class="tbl">
            <thead>
              <tr>
                <th>클래스</th>
                <th>메서드</th>
                <th>@Transactional</th>
                <th>AOP 로깅</th>
              </tr>
            </thead>
            <tbody>
              <template v-for="[cls, methods] in servicesByClass" :key="cls">
                <tr v-for="(m, i) in methods" :key="`${cls}.${m.method}`">
                  <td class="mono">{{ i === 0 ? cls : '' }}</td>
                  <td class="mono">{{ m.method }}</td>
                  <td>
                    <span v-if="m.transactional" class="tag" :class="{ ro: m.readOnly }">
                      {{ m.readOnly ? 'readOnly' : '쓰기' }}
                    </span>
                    <span v-else class="dim">—</span>
                  </td>
                  <td>
                    <span v-if="m.aopLogged" class="tick">●</span>
                    <span v-else class="dim" title="포인트컷에서 제외됨">제외</span>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </section>

      <!-- ── ERD ── -->
      <section class="section">
        <header class="sec-head">
          <h2 class="sec-title">DB 구조</h2>
          <span class="muted n">JPA Metamodel 기준</span>
        </header>

        <div class="erd">
          <div v-for="entity in data.entities" :key="entity.name" class="card ent">
            <header class="ent-head">
              <span class="ent-name">{{ entity.name }}</span>
              <span class="mono dim ent-table">{{ entity.tableName }}</span>
            </header>
            <ul class="attrs">
              <li v-for="a in entity.attributes" :key="a.name" class="attr">
                <span class="key" :class="{ pk: a.id, fk: !!a.targetEntity }">
                  {{ a.id ? 'PK' : a.targetEntity ? 'FK' : '' }}
                </span>
                <span class="mono attr-name">{{ a.name }}</span>
                <span class="mono dim attr-type">
                  {{ a.type }}<template v-if="!a.optional && !a.id"> ·&nbsp;NOT NULL</template>
                </span>
                <span v-if="a.targetEntity" class="rel">→ {{ a.targetEntity }}</span>
              </li>
            </ul>
          </div>
        </div>
      </section>
    </template>
  </StateBlock>
</template>

<style scoped>
.lead-note {
  margin: 0 0 22px;
  font-size: 0.83rem;
  max-width: 68ch;
  line-height: 1.6;
}

.section {
  margin-bottom: 34px;
}

.section:last-child {
  margin-bottom: 72px;
}

.sec-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.sec-title {
  font-size: 1.02rem;
  font-weight: 500;
  letter-spacing: -0.02em;
}

.n {
  font-size: 0.8rem;
}

/* ── 표 ── */

.table-wrap {
  overflow-x: auto;
}

.tbl {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.83rem;
}

.tbl th {
  text-align: left;
  font-weight: 500;
  font-size: 0.72rem;
  letter-spacing: 0.05em;
  color: var(--ink-muted);
  padding: 12px 14px;
  border-bottom: 1px solid var(--line);
  white-space: nowrap;
}

.tbl td {
  padding: 9px 14px;
  border-bottom: 1px solid var(--line);
  vertical-align: top;
}

.tbl tbody tr:last-child td {
  border-bottom: 0;
}

.tbl tbody tr:hover {
  background: var(--surface-sunken);
}

.w-method {
  width: 1%;
}

/* 리소스 묶음 구분 행 */
.group-row td {
  padding: 9px 14px 5px;
  background: var(--surface-sunken);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 0.72rem;
  letter-spacing: 0.03em;
  color: var(--ink-soft);
  border-bottom: 1px solid var(--line);
}

/* 경로·핸들러는 길어지면 접힌다 (가로 스크롤 대신 두 줄) */
.col-path,
.col-handler {
  overflow-wrap: anywhere;
  line-height: 1.45;
}

.col-path {
  max-width: 250px;
}

.col-handler {
  max-width: 190px;
}

/* 기능은 항상 한 줄로 — 줄바꿈되면 표가 읽기 어려워진다 */
.col-desc {
  white-space: nowrap;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 0.78rem;
}

.dim {
  color: var(--ink-muted);
}

.verb {
  display: inline-block;
  padding: 2px 8px;
  border-radius: var(--r-sm);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 0.7rem;
  font-weight: 600;
  background: var(--surface-sunken);
  color: var(--ink-soft);
}

.verb--get {
  background: var(--sky-wash);
  color: var(--sky-ink);
}

.verb--post {
  background: var(--mint-wash);
  color: var(--mint-deep);
}

.verb--patch,
.verb--put {
  background: var(--amber-wash);
  color: var(--amber-ink);
}

.verb--delete {
  background: var(--danger-wash);
  color: var(--danger);
}

.tag {
  display: inline-block;
  margin-right: 4px;
  padding: 2px 8px;
  border-radius: 999px;
  border: 1px solid var(--line-strong);
  font-size: 0.72rem;
  color: var(--ink-soft);
  white-space: nowrap;
}

.tag.ro {
  border-color: var(--line);
  color: var(--ink-muted);
}

.tick {
  color: var(--mint);
  font-size: 0.7rem;
}

/* ── ERD ── */

.erd {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
  align-items: start;
}

.ent {
  overflow: hidden;
}

.ent-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
  padding: 12px 16px;
  background: var(--surface-accent);
  border-bottom: 1px solid var(--line);
}

.ent-name {
  font-weight: 600;
  font-size: 0.92rem;
  letter-spacing: -0.01em;
}

.ent-table {
  font-size: 0.74rem;
}

.attrs {
  list-style: none;
  margin: 0;
  padding: 6px 0;
}

.attr {
  display: grid;
  grid-template-columns: 26px minmax(0, 1fr) auto;
  align-items: baseline;
  gap: 8px;
  padding: 4px 16px;
}

.attr:hover {
  background: var(--surface-sunken);
}

.key {
  font-size: 0.62rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: transparent;
}

.key.pk {
  color: var(--mint-deep);
}

.key.fk {
  color: var(--amber-ink);
}

.attr-name {
  font-size: 0.8rem;
}

.attr-type {
  font-size: 0.72rem;
  text-align: right;
  white-space: nowrap;
}

.rel {
  grid-column: 2 / -1;
  font-size: 0.72rem;
  color: var(--amber-ink);
}
</style>
