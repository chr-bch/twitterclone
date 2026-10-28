<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchFeed } from '@/api'
import { currentUser, login, logout } from '@/auth'
import type { Post } from '@/types'
import PostCard from '@/components/PostCard.vue'
import PostComposer from '@/components/PostComposer.vue'

const posts = ref<Post[]>([])
const nextCursor = ref<string | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

async function load(cursor?: string) {
  loading.value = true
  error.value = null
  try {
    const feed = await fetchFeed(cursor)
    posts.value = cursor ? [...posts.value, ...feed.posts] : feed.posts
    nextCursor.value = feed.nextCursor
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Feed konnte nicht geladen werden'
  } finally {
    loading.value = false
  }
}

const prepend = (post: Post) => posts.value.unshift(post)

onMounted(() => load())
</script>

<template>
  <div class="min-h-screen bg-slate-50 text-slate-900 dark:bg-slate-950 dark:text-slate-100">
    <div
      class="mx-auto min-h-screen max-w-xl border-x border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900"
    >
      <header
        class="sticky top-0 z-10 flex items-center justify-between border-b border-slate-200 bg-white/80 px-4 py-3 backdrop-blur dark:border-slate-800 dark:bg-slate-900/80"
      >
        <h1 class="text-xl font-bold">Twitterclone</h1>
        <div v-if="currentUser" class="flex items-center gap-3 text-sm">
          <span class="text-slate-500">@{{ currentUser.username }}</span>
          <button
            class="rounded-full border border-slate-300 px-4 py-1.5 font-semibold transition hover:bg-slate-100 dark:border-slate-700 dark:hover:bg-slate-800"
            @click="logout"
          >
            Abmelden
          </button>
        </div>
        <button
          v-else
          class="rounded-full bg-sky-500 px-4 py-1.5 text-sm font-semibold text-white transition hover:bg-sky-600"
          @click="login"
        >
          Anmelden
        </button>
      </header>

      <main class="divide-y divide-slate-200 dark:divide-slate-800">
        <PostComposer v-if="currentUser" @created="prepend" />
        <p v-else class="px-4 py-4 text-sm text-slate-500">
          <button class="font-semibold text-sky-600 hover:underline" @click="login">
            Melde dich an</button
          >, um selbst zu posten.
        </p>

        <PostCard v-for="post in posts" :key="post.id" :post="post" />

        <div class="px-4 py-6 text-center text-sm">
          <p v-if="error" class="mb-3 text-rose-600">{{ error }}</p>
          <p v-else-if="!loading && posts.length === 0" class="text-slate-500">
            Noch keine Beiträge.
          </p>
          <button
            v-if="error || nextCursor"
            :disabled="loading"
            class="rounded-full border border-slate-300 px-4 py-2 font-semibold transition hover:bg-slate-100 disabled:opacity-50 dark:border-slate-700 dark:hover:bg-slate-800"
            @click="load(error ? undefined : nextCursor!)"
          >
            {{ loading ? 'Lädt…' : error ? 'Erneut versuchen' : 'Mehr laden' }}
          </button>
          <p v-else-if="loading" class="text-slate-500">Lädt…</p>
        </div>
      </main>
    </div>
  </div>
</template>
