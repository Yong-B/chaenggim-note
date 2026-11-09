import type { ReactNode } from 'react'

/** 빈 상태 문구 (문구는 06 문서 각 절). */
export function EmptyState({ children }: { children: ReactNode }) {
  return <p className="empty">{children}</p>
}
