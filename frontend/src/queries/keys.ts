import type { ProductTag, QuestionStatus } from '../api/types'

export const keys = {
  home: ['home'] as const,
  doctors: (q: string) => ['doctors', q] as const,
  doctor: (id: number) => ['doctor', id] as const,
  visit: (id: number) => ['visit', id] as const,
  questions: (status: QuestionStatus, product: ProductTag | null) => ['questions', status, product ?? 'ALL'] as const,
  question: (id: number) => ['question', id] as const,
  commitment: (id: number) => ['commitment', id] as const,
}
