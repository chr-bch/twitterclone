import { ref } from 'vue'

const now = ref(Date.now())
setInterval(() => (now.value = Date.now()), 30_000)

const relative = new Intl.RelativeTimeFormat('de', { numeric: 'auto' })
const absolute = new Intl.DateTimeFormat('de', { dateStyle: 'medium', timeStyle: 'short' })

export function timeAgo(iso: string) {
  const seconds = Math.round((new Date(iso).getTime() - now.value) / 1000)
  if (seconds > -60) return 'gerade eben'
  if (seconds > -3600) return relative.format(Math.round(seconds / 60), 'minute')
  if (seconds > -86400) return relative.format(Math.round(seconds / 3600), 'hour')
  if (seconds > -7 * 86400) return relative.format(Math.round(seconds / 86400), 'day')
  return absolute.format(new Date(iso))
}

export const fullDate = (iso: string) => absolute.format(new Date(iso))
