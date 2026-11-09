const WEEK = ['일', '월', '화', '수', '목', '금', '토']

const pad = (n: number) => String(n).padStart(2, '0')

/** "2026-11-09" → Date(로컬). new Date("2026-11-09")는 UTC로 해석되므로 쓰지 않는다. */
function parseDate(d: string): Date {
  const [y, m, day] = d.slice(0, 10).split('-').map(Number)
  return new Date(y, m - 1, day)
}

/** "2026-11-09" → "2026.11.09 (월)" */
export function formatDate(d: string): string {
  const date = parseDate(d)
  return `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(date.getDate())} (${WEEK[date.getDay()]})`
}

/** "2026-11-09T16:00:00(.sss)" → "2026.11.09 16:00" */
export function formatDateTime(dt: string): string {
  const [datePart, timePart = ''] = dt.split('T')
  const [y, m, d] = datePart.split('-')
  return `${y}.${m}.${d} ${timePart.slice(0, 5)}`
}

/** 브라우저 로컬 날짜 "YYYY-MM-DD" */
export function todayString(): string {
  const now = new Date()
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
}
