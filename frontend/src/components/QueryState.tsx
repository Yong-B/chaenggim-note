import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { ApiError } from '../api/client'

type QueryLike<T> = {
  data: T | undefined
  error: Error | null
  isPending: boolean
  refetch: () => unknown
}

/**
 * 06 문서 0.3절 로딩·에러 공통 처리.
 * 데이터가 있으면(다시 가져오기 실패 포함) 그대로 보여 주고, 없을 때만 로딩·404·에러를 보여 준다.
 */
export function QueryState<T>({ query, children }: { query: QueryLike<T>; children: (data: T) => ReactNode }) {
  if (query.data !== undefined) {
    return <>{children(query.data)}</>
  }
  if (query.error instanceof ApiError && query.error.status === 404) {
    return (
      <div className="query-state">
        <p>요청한 정보를 찾을 수 없습니다.</p>
        <Link to="/">홈으로</Link>
      </div>
    )
  }
  if (query.error) {
    return (
      <div className="query-state" role="alert">
        <p>데이터를 불러오지 못했습니다.</p>
        <button type="button" className="btn" onClick={() => query.refetch()}>
          다시 시도
        </button>
      </div>
    )
  }
  return (
    <div className="query-state" aria-live="polite">
      <p>불러오는 중…</p>
    </div>
  )
}
