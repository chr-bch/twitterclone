<script setup lang="ts">
import { computed, ref } from 'vue'
import { createPost } from '@/api'
import { currentUser } from '@/auth'
import type { Post } from '@/types'
import AppAvatar from './AppAvatar.vue'

const MAX_LENGTH = 280

const emit = defineEmits<{ created: [post: Post] }>()

const content = ref('')
const sending = ref(false)
const error = ref<string | null>(null)

const length = computed(() => [...content.value].length)
const remaining = computed(() => MAX_LENGTH - length.value)
const canSubmit = computed(
  () => content.value.trim().length > 0 && remaining.value >= 0 && !sending.value,
)

const RADIUS = 9
const CIRCUMFERENCE = 2 * Math.PI * RADIUS
const progressOffset = computed(() => CIRCUMFERENCE * (1 - Math.min(length.value / MAX_LENGTH, 1)))
const progressColor = computed(() =>
  remaining.value < 0 ? 'text-rose-500' : remaining.value <= 20 ? 'text-amber-500' : 'text-sky-500',
)

async function submit() {
  if (!canSubmit.value) return
  sending.value = true
  error.value = null
  try {
    emit('created', await createPost(content.value))
    content.value = ''
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Unbekannter Fehler'
  } finally {
    sending.value = false
  }
}
</script>

<template>
  <form class="flex gap-3 px-4 py-4" @submit.prevent="submit">
    <AppAvatar :name="currentUser!.displayName" />
    <div class="flex-1">
      <textarea
        v-model="content"
        rows="2"
        placeholder="Was gibt's Neues?"
        aria-label="Neuer Beitrag"
        class="field-sizing-content min-h-16 w-full resize-none bg-transparent text-lg outline-none placeholder:text-slate-400"
        @keydown.meta.enter="submit"
        @keydown.ctrl.enter="submit"
      />
      <p v-if="error" class="mb-2 text-sm text-rose-600">{{ error }}</p>
      <div
        class="flex items-center justify-end gap-3 border-t border-slate-200 pt-3 dark:border-slate-800"
      >
        <div v-if="length > 0" class="flex items-center gap-2">
          <span
            v-if="remaining <= 20"
            :class="remaining < 0 ? 'text-rose-500' : 'text-slate-500'"
            class="text-sm tabular-nums"
          >
            {{ remaining }}
          </span>
          <svg class="size-6 -rotate-90" viewBox="0 0 24 24" aria-hidden="true">
            <circle
              cx="12"
              cy="12"
              :r="RADIUS"
              fill="none"
              stroke-width="2.5"
              class="stroke-slate-200 dark:stroke-slate-700"
            />
            <circle
              cx="12"
              cy="12"
              :r="RADIUS"
              fill="none"
              stroke="currentColor"
              stroke-width="2.5"
              stroke-linecap="round"
              :stroke-dasharray="CIRCUMFERENCE"
              :stroke-dashoffset="progressOffset"
              :class="progressColor"
              class="transition-[stroke-dashoffset] duration-150"
            />
          </svg>
        </div>
        <button
          type="submit"
          :disabled="!canSubmit"
          class="rounded-full bg-sky-500 px-5 py-2 font-semibold text-white transition hover:bg-sky-600 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {{ sending ? 'Sende…' : 'Posten' }}
        </button>
      </div>
    </div>
  </form>
</template>
