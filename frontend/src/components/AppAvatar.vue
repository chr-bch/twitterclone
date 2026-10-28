<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ name: string }>()

const colors = ['bg-sky-500', 'bg-violet-500', 'bg-emerald-500', 'bg-amber-500', 'bg-rose-500']
const color = computed(
  () => colors[[...props.name].reduce((sum, c) => sum + c.codePointAt(0)!, 0) % colors.length],
)
</script>

<template>
  <div
    :class="color"
    class="flex size-10 shrink-0 items-center justify-center rounded-full font-semibold text-white"
  >
    {{ [...name][0]?.toUpperCase() }}
  </div>
</template>
