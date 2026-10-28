<script setup lang="ts">
import { fullDate, timeAgo } from '@/time'
import type { Post } from '@/types'
import AppAvatar from './AppAvatar.vue'

defineProps<{ post: Post }>()
</script>

<template>
  <article class="flex gap-3 px-4 py-4">
    <AppAvatar :name="post.author.displayName" />
    <div class="min-w-0 flex-1">
      <div class="flex flex-wrap items-baseline gap-x-1.5 text-sm">
        <span class="font-semibold">{{ post.author.displayName }}</span>
        <span class="text-slate-500">@{{ post.author.username }}</span>
        <span class="text-slate-400">·</span>
        <time :datetime="post.createdAt" :title="fullDate(post.createdAt)" class="text-slate-500">
          {{ timeAgo(post.createdAt) }}
        </time>
      </div>
      <p class="mt-1 break-words whitespace-pre-wrap">{{ post.content }}</p>
    </div>
  </article>
</template>
