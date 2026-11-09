import type { ReactNode } from 'react'
import type { BadgeTone } from '../lib/labels'

/** 의미 색 배지. 색만으로 구분하지 않도록 항상 글자를 넣는다 (07 문서 5절). */
export function Badge({ tone, children }: { tone: BadgeTone; children: ReactNode }) {
  return <span className={`badge badge--${tone}`}>{children}</span>
}
