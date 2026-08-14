<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import BrandMark from '@/components/BrandMark.vue'
</script>

<template>
  <div class="shell">
    <header class="topbar">
      <div class="container bar">
        <RouterLink to="/" class="brand">
          <BrandMark :size="22" />
          ClassTrack
        </RouterLink>

        <nav class="nav">
          <RouterLink to="/">대시보드</RouterLink>
          <!-- 강의 상세(/courses/1)에서도 "강의" 탭이 켜지도록 exact 대신 부분 일치를 쓴다 -->
          <RouterLink to="/courses" class="section-link">강의</RouterLink>
          <RouterLink to="/assignments">과제</RouterLink>
          <RouterLink to="/portfolio">포트폴리오</RouterLink>
        </nav>
      </div>
    </header>

    <main class="container">
      <RouterView />
    </main>

    <footer class="footer">
      <div class="container foot">
        <RouterLink to="/dev" class="logs-link">dev →</RouterLink>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.shell {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(247, 250, 249, 0.82);
  backdrop-filter: saturate(180%) blur(12px);
  border-bottom: 1px solid var(--line);
}

.bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  height: 62px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  font-size: 0.98rem;
  font-weight: 600;
  letter-spacing: -0.02em;
}


.nav {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.88rem;
}

.nav a {
  padding: 7px 14px;
  border-radius: 999px;
  color: var(--ink-soft);
  transition:
    background 0.16s var(--ease),
    color 0.16s var(--ease);
}

.nav a:hover {
  background: var(--surface);
  color: var(--ink);
}

.nav a.router-link-exact-active,
.nav a.section-link.router-link-active {
  color: var(--ink);
  background: var(--surface);
  box-shadow: inset 0 0 0 1px var(--line);
}

main {
  flex: 1;
}

.footer {
  border-top: 1px solid var(--line);
  margin-top: auto;
}

.foot {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 62px;
  font-size: 0.8rem;
}

.logs-link {
  color: var(--ink-muted);
  transition: color 0.16s var(--ease);
}

.logs-link:hover {
  color: var(--mint-deep);
}
</style>
