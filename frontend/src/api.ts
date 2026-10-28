import { accessToken } from './auth'
import type { Feed, Post } from './types'

export class ApiError extends Error {}

async function toError(response: Response) {
  const problem = await response.json().catch(() => ({}))
  const fieldError = problem.errors && Object.values(problem.errors)[0]
  return new ApiError(fieldError ?? problem.detail ?? problem.title ?? `HTTP ${response.status}`)
}

export async function fetchFeed(cursor?: string): Promise<Feed> {
  const query = cursor ? `?cursor=${encodeURIComponent(cursor)}` : ''
  const response = await fetch(`/api/posts${query}`)
  if (!response.ok) throw await toError(response)
  return response.json()
}

export async function createPost(content: string): Promise<Post> {
  const response = await fetch('/api/posts', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${await accessToken()}`,
    },
    body: JSON.stringify({ content }),
  })
  if (!response.ok) throw await toError(response)
  return response.json()
}
