import { Link, useSearchParams } from 'react-router-dom'
import type { QuestionStatus } from '../../api/types'
import { EmptyState } from '../../components/EmptyState'
import { PageTitle } from '../../components/PageTitle'
import { QueryState } from '../../components/QueryState'
import { formatDate, formatDateTime } from '../../lib/date'
import { isQuestionStatus, QUESTION_STATUSES, statusLabel } from '../../lib/labels'
import { useQuestionsQuery } from '../../queries/questions'

/** 06 화면 명세 7절 — 질문함. 탭은 URL ?status= 에 둔다(없거나 잘못된 값이면 미답변). */
export function QuestionListPage() {
  const [params] = useSearchParams()
  const raw = params.get('status')
  const status: QuestionStatus = isQuestionStatus(raw) ? raw : 'UNANSWERED'
  const query = useQuestionsQuery(status)

  return (
    <>
      <PageTitle title="질문함" />
      <nav className="tabs" aria-label="질문 상태">
        {QUESTION_STATUSES.map((s) => (
          <Link
            key={s}
            to={`/questions?status=${s}`}
            className={s === status ? 'tabs__item tabs__item--active' : 'tabs__item'}
            aria-current={s === status ? 'page' : undefined}
          >
            {statusLabel[s]} {query.data ? query.data.counts[s] : ''}
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
