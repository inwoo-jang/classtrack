<script setup lang="ts">
/**
 * 헤더용 앱 아이콘.
 *
 * public/icon-64.png 를 쓴다. 22px 로 표시하므로 64px 원본이면 3x 화면까지 선명하다.
 */
withDefaults(defineProps<{ size?: number }>(), { size: 22 })
</script>

<template>
  <img
    class="mark"
    src="/icon-64.png"
    :width="size"
    :height="size"
    alt=""
    aria-hidden="true"
    decoding="async"
  />
</template>

<style scoped>
.mark {
  display: block;
  flex-shrink: 0;
  border-radius: 5px;
}
</style>
