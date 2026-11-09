import type { ProductTag, QuestionStatus, SourceType } from '../api/types'

export type BadgeTone = 'danger' | 'warn' | 'ok' | 'muted'

export const statusLabel: Record<QuestionStatus, string> = {
  UNANSWERED: '미답변',
  CHECKING: '확인 중',
  READY: '답변 준비됨',
  DELIVERED: '전달 완료',
}

export const statusTone: Record<QuestionStatus, BadgeTone> = {
  UNANSWERED: 'danger',
  CHECKING: 'warn',
  READY: 'ok',
  DELIVERED: 'muted',
}

export const sourceLabel: Record<SourceType, string> = {
  LABEL: '허가사항',
  CLINICAL: '임상자료',
  MEDICAL_TEAM: '학술팀 회신',
}

/** P1 */
export const productLabel: Record<ProductTag, string> = {
  ROSUZET: '로수젯',
  AMOSARTAN: '아모잘탄',
  AMOPREL: '아모프렐',
  EFE: '에페',
  ROLONTIS: '롤론티스',
}

export const PRODUCT_TAGS: ProductTag[] = ['ROSUZET', 'AMOSARTAN', 'AMOPREL', 'EFE', 'ROLONTIS']

export function isProductTag(value: string | null): value is ProductTag {
  return value !== null && (PRODUCT_TAGS as string[]).includes(value)
}

/** 탭·필터 순서 */
export const QUESTION_STATUSES: QuestionStatus[] = ['UNANSWERED', 'CHECKING', 'READY', 'DELIVERED']

export function isQuestionStatus(value: string | null): value is QuestionStatus {
  return value !== null && (QUESTION_STATUSES as string[]).includes(value)
}

/** 약속 배지: overdue → 기한 지남/danger, dueToday → 오늘까지/warn, 그 외 없음 */
export function commitmentBadge(c: { overdue: boolean; dueToday: boolean }): { label: string; tone: BadgeTone } | null {
  if (c.overdue) return { label: '기한 지남', tone: 'danger' }
  if (c.dueToday) return { label: '오늘까지', tone: 'warn' }
  return null
}

/** 의료진 pastDue → 예정일 지남/muted */
export const PAST_DUE_BADGE = { label: '예정일 지남', tone: 'muted' } as const satisfies { label: string; tone: BadgeTone }
