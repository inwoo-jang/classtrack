<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { subjectColor } from '@/utils/subjectColor'

/**
 * 과목 선택 콤보박스.
 *
 * 네이티브 select 를 쓰지 않은 이유: 옵션 목록의 위치와 모양을 브라우저가 정해서
 * 입력칸과 어긋나 보인다. 직접 그리면 필드 바로 아래에 딱 맞춰 띄울 수 있다.
 */
const props = defineProps<{
  modelValue: string
  options: string[]
  id?: string
}>()

const emit = defineEmits<{ 'update:modelValue': [string] }>()

const open = ref(false)
const custom = ref(false)
const activeIndex = ref(-1)

const root = ref<HTMLElement | null>(null)
const customInput = ref<HTMLInputElement | null>(null)

/** 목록에 없는 값이 이미 들어와 있으면(수정 화면) 처음부터 입력 모드로 연다. */
watch(
  () => [props.modelValue, props.options] as const,
  ([value, options]) => {
    if (value && options.length && !options.includes(value)) {
      custom.value = true
    }
  },
  { immediate: true },
)

const label = computed(() => props.modelValue || '과목 선택')

function toggle() {
  open.value = !open.value
  if (open.value) {
    activeIndex.value = props.options.indexOf(props.modelValue)
    document.addEventListener('pointerdown', onOutside, true)
  } else {
    document.removeEventListener('pointerdown', onOutside, true)
  }
}

function close() {
  open.value = false
  document.removeEventListener('pointerdown', onOutside, true)
}

function onOutside(event: PointerEvent) {
  if (root.value && !root.value.contains(event.target as Node)) {
    close()
  }
}

function pick(value: string) {
  emit('update:modelValue', value)
  custom.value = false
  close()
}

async function pickCustom() {
  custom.value = true
  emit('update:modelValue', '')
  close()
  await nextTick()
  customInput.value?.focus()
}

function backToList() {
  custom.value = false
  emit('update:modelValue', '')
}

/** 위/아래로 항목 이동, Enter 로 선택, Esc 로 닫기. */
function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    close()
    return
  }
  if (!open.value && (event.key === 'ArrowDown' || event.key === 'Enter')) {
    event.preventDefault()
    toggle()
    return
  }
  if (!open.value) return

  const last = props.options.length // 마지막 인덱스는 "직접 입력"

  if (event.key === 'ArrowDown') {
    event.preventDefault()
    activeIndex.value = activeIndex.value >= last ? 0 : activeIndex.value + 1
  } else if (event.key === 'ArrowUp') {
    event.preventDefault()
    activeIndex.value = activeIndex.value <= 0 ? last : activeIndex.value - 1
  } else if (event.key === 'Enter') {
    event.preventDefault()
    if (activeIndex.value === last) pickCustom()
    else if (activeIndex.value >= 0) pick(props.options[activeIndex.value]!)
  }
}

onBeforeUnmount(() => document.removeEventListener('pointerdown', onOutside, true))
</script>

<template>
  <div v-if="!custom" ref="root" class="combo">
    <button
      :id="id"
      type="button"
      class="input trigger"
      :class="{ empty: !modelValue, open }"
      aria-haspopup="listbox"
      :aria-expanded="open"
      @click="toggle"
      @keydown="onKeydown"
    >
      <span v-if="modelValue" class="swatch" :style="{ background: subjectColor(modelValue) }" />
      <span class="label">{{ label }}</span>
      <span class="caret" :class="{ up: open }" aria-hidden="true" />
    </button>

    <ul v-if="open" class="panel" role="listbox">
      <li
        v-for="(o, i) in options"
        :key="o"
        role="option"
        :aria-selected="o === modelValue"
        class="opt"
        :class="{ active: i === activeIndex, on: o === modelValue }"
        @pointerenter="activeIndex = i"
        @click="pick(o)"
      >
        <span class="swatch" :style="{ background: subjectColor(o) }" />
        {{ o }}
      </li>

      <li
        class="opt custom-opt"
        :class="{ active: activeIndex === options.length }"
        @pointerenter="activeIndex = options.length"
        @click="pickCustom"
      >
        + 직접 입력
      </li>
    </ul>
  </div>

  <div v-else class="custom">
    <input
      :id="id"
      ref="customInput"
      class="input"
      :value="modelValue"
      required
      placeholder="새 과목명"
      @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    />
    <button class="back" type="button" @click="backToList">목록에서 선택</button>
  </div>
</template>

<style scoped>
.combo {
  position: relative;
}

.trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  text-align: left;
  cursor: pointer;
}

.trigger.empty .label {
  color: #b0b3c0;
}

.trigger.open {
  border-color: var(--mint);
  box-shadow: 0 0 0 3px var(--mint-wash);
}

.label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.swatch {
  flex-shrink: 0;
  width: 8px;
  height: 8px;
  border-radius: 2px;
}

.caret {
  flex-shrink: 0;
  width: 7px;
  height: 7px;
  margin-top: -3px;
  border-right: 1.5px solid var(--ink-muted);
  border-bottom: 1.5px solid var(--ink-muted);
  transform: rotate(45deg);
  transition: transform 0.16s var(--ease);
}

.caret.up {
  margin-top: 3px;
  transform: rotate(-135deg);
}

/* 입력칸 바로 아래, 같은 너비로 */
.panel {
  position: absolute;
  z-index: 20;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  max-height: 264px;
  overflow-y: auto;
  margin: 0;
  padding: 4px;
  list-style: none;
  background: var(--surface);
  border: 1px solid var(--line-strong);
  border-radius: var(--r-md);
  box-shadow: var(--shadow-md);
}

.opt {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  border-radius: var(--r-sm);
  font-size: 0.88rem;
  color: var(--ink-soft);
  cursor: pointer;
}

.opt.active {
  background: var(--surface-sunken);
  color: var(--ink);
}

.opt.on {
  font-weight: 500;
  color: var(--ink);
}

.custom-opt {
  margin-top: 4px;
  padding-top: 9px;
  border-top: 1px solid var(--line);
  border-radius: 0 0 var(--r-sm) var(--r-sm);
  color: var(--ink-muted);
}

.custom {
  display: flex;
  align-items: center;
  gap: 8px;
}

.custom .input {
  flex: 1;
  min-width: 0;
}

.back {
  flex-shrink: 0;
  padding: 0;
  border: 0;
  background: none;
  font-size: 0.76rem;
  color: var(--ink-muted);
  cursor: pointer;
  white-space: nowrap;
  transition: color 0.16s var(--ease);
}

.back:hover {
  color: var(--ink);
}
</style>
