<script setup lang="ts">
/** dev 영역 공통 헤더. 구현 현황 / 로그 두 탭을 오간다. */
</script>

<template>
  <header class="dev-head">
    <h1 class="dev-title">dev</h1>
    <nav class="tabs">
      <RouterLink to="/dev">구현 현황</RouterLink>
      <RouterLink to="/dev/logs">로그 확인하기</RouterLink>
    </nav>
  </header>
</template>

<style scoped>
.dev-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 36px 0 18px;
}

.dev-title {
  font-size: 1.85rem;
  font-weight: 500;
  letter-spacing: -0.03em;
}

.tabs {
  display: flex;
  gap: 6px;
}

.tabs a {
  padding: 7px 15px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 0.86rem;
  color: var(--ink-soft);
  transition:
    background 0.16s var(--ease),
    border-color 0.16s var(--ease),
    color 0.16s var(--ease);
}

.tabs a:hover {
  background: var(--surface);
  border-color: var(--line-strong);
}

.tabs a.router-link-exact-active {
  background: var(--mint-ink);
  border-color: var(--mint-ink);
  color: #fff;
}
</style>
