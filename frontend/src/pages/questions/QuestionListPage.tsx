import { Link, useSearchParams } from 'react-router-dom'
import type { ProductTag, QuestionStatus } from '../../api/types'
import { EmptyState } from '../../components/EmptyState'
import { PageTitle } from '../../components/PageTitle'
import { ProductTagLabel } from '../../components/ProductTagLabel'
import { QueryState } from '../../components/QueryState'
import { formatDate, formatDateTime } from '../../lib/date'
import { isProductTag, isQuestionStatus, PRODUCT_TAGS, productLabel, QUESTION_STATUSES, statusLabel } from '../../lib/labels'
import { useQuestionsQuery } from '../../queries/questions'

/** 탭·필터 링크 주소: 상태와 제품을 함께 유지한다. */
function listPath(status: QuestionStatus, product: ProductTag | null): string {
  return `/questions?status=${status}${product ? `&product=${product}` : ''}`
}

/**
 * 06 화면 명세 7절 — 질문함. 상태 탭(?status=)과 제품 필터(?product=, P1)는 URL에 두고 함께 동작한다.
 * 없거나 잘못된 status는 미답변, 없거나 잘못된 product는 전체.
 */
export function QuestionListPage() {
  const [params] = useSearchParams()
  const rawStatus = params.get('status')
  const rawProduct = params.get('product')
  const status: QuestionStatus = isQuestionStatus(rawStatus) ? rawStatus : 'UNANSWERED'
  const product: ProductTag | null = isProductTag(rawProduct) ? rawProduct : null
  const query = useQuestionsQuery(status, product)

  return (
    <>
      <PageTitle title="질문함" />
      <nav className="tabs" aria-label="질문 상태">
        {QUESTION_STATUSES.map((s) => (
          <Link
            key={s}
            to={listPath(s, product)}
            className={s === status ? 'tabs__item tabs__item--active' : 'tabs__item'}
            aria-current={s === status ? 'page' : undefined}
          >
            {statusLabel[s]} {query.data ? query.data.counts[s] : ''}
          </Link>
        ))}
      </nav>
      <nav className="filters" aria-label="제품">
        {[null, ...PRODUCT_TAGS].map((p) => (
          <Link
            key={p ?? 'ALL'}
            to={listPath(status, p)}
            className={p === product ? 'filter filter--active' : 'filter'}
            aria-current={p === product ? 'true' : undefined}
          >
            {p ? productLabel[p] : '전체'}
          </Link>
        ))}
      </nav>
      <QueryState query={query}>
        {(data) =>
          data.items.length === 0 ? (
            <EmptyState>{statusLabel[status]} 질문이 없습니다.</EmptyState>
          ) : (
            <ul className="list">
              {data.items.map((q) => (
                <li key={q.id} className="row">
                  <Link className="row__main" to={`/questions/${q.id}`}>
                    {q.productTag && (
                      <span className="row__badges">
                        <ProductTagLabel tag={q.productTag} />
                      </span>
                    )}
                    <span className="row__title">{q.content}</span>
                    <span className="row__sub">
                      {q.doctor.name} · {q.doctor.hospital}
                    </span>
                    <span className="row__sub">
                      {status === 'DELIVERED' && q.deliveredAt
                        ? `전달 ${formatDateTime(q.deliveredAt)}`
                        : `받은 날 ${formatDate(q.visitDate)}`}
                    </span>
                  </Link>
                </li>
              ))}
            </ul>
          )
        }
      </QueryState>
    </>
  )
}
