import { Link } from 'react-router-dom'
import type { CommitmentResponse, HomeResponse, UpcomingDoctor } from '../api/types'
import { Badge } from '../components/Badge'
import { EmptyState } from '../components/EmptyState'
import { PageTitle } from '../components/PageTitle'
import { QueryState } from '../components/QueryState'
import { useToast } from '../components/Toast'
import { formatDate } from '../lib/date'
import { errorMessage } from '../lib/errors'
import { PAST_DUE_BADGE, statusLabel } from '../lib/labels'
import { useCompleteCommitmentMutation } from '../queries/commitments'
import { useHomeQuery, useResetMutation } from '../queries/home'

/** 06 화면 명세 1절 — 홈 "오늘 챙길 것". */
export function HomePage() {
  const query = useHomeQuery()

  return (
    <>
      {/* ① 날짜 줄은 제목 바로 아래(서버 today 기준, 불러온 뒤 표시) */}
      <PageTitle title="오늘 챙길 것" lead={query.data ? `오늘 ${formatDate(query.data.today)}` : undefined} />
      <QueryState query={query}>{(data) => <HomeContent data={data} />}</QueryState>
    </>
  )
}

function HomeContent({ data }: { data: HomeResponse }) {
  return (
    <>
      <div className="home-grid">
        <div className="home-grid__left">
          <section className="section" aria-labelledby="home-overdue">
            <h2 className="section__title" id="home-overdue">
              기한 지난 약속
            </h2>
            {data.overdueCommitments.length === 0 ? (
              <EmptyState>기한 지난 약속이 없습니다.</EmptyState>
            ) : (
              <CommitmentList items={data.overdueCommitments} badge={{ label: '기한 지남', tone: 'danger' }} />
            )}
          </section>

          {data.dueTodayCommitments.length > 0 && (
            <section className="section" aria-labelledby="home-due-today">
              <h2 className="section__title" id="home-due-today">
                오늘까지인 약속
              </h2>
              <CommitmentList items={data.dueTodayCommitments} badge={{ label: '오늘까지', tone: 'warn' }} />
            </section>
          )}
        </div>

        <section className="section home-grid__right" aria-labelledby="home-upcoming">
          <h2 className="section__title" id="home-upcoming">
            이번 주 방문 예정
          </h2>
          {data.upcomingDoctors.length === 0 ? (
            <EmptyState>이번 주 방문 예정인 의료진이 없습니다.</EmptyState>
          ) : (
            <ul className="list">
              {data.upcomingDoctors.map((u) => (
                <UpcomingRow key={u.doctor.id} item={u} />
              ))}
            </ul>
          )}
        </section>

        <section className="section home-grid__full" aria-labelledby="home-questions">
          <h2 className="section__title" id="home-questions">
            질문 현황
          </h2>
          <div className="stats">
            {(['UNANSWERED', 'CHECKING', 'READY'] as const).map((status) => (
              <Link key={status} className="stat" to={`/questions?status=${status}`}>
                <span className="stat__num">{data.questionCounts[status]}</span>
                <span className="stat__label">{statusLabel[status]}</span>
              </Link>
            ))}
          </div>
        </section>

        <div className="home-grid__full">
          <ResetButton />
        </div>
      </div>
    </>
  )
}

function CommitmentList({
  items,
  badge,
}: {
  items: CommitmentResponse[]
  badge: { label: string; tone: 'danger' | 'warn' }
}) {
  const toast = useToast()
  const complete = useCompleteCommitmentMutation()

  const onComplete = (id: number) => {
    complete.mutate(id, {
      onSuccess: () => toast.show('약속을 완료했습니다.'),
      onError: (e) => toast.show(errorMessage(e), 'danger'),
    })
  }

  return (
    <ul className="list">
      {items.map((c) => (
        <li key={c.id} className="row">
          <Link className="row__main" to={`/doctors/${c.doctor.id}`}>
            <span className="row__badges">
              <Badge tone={badge.tone}>{badge.label}</Badge>
            </span>
            <span className="row__title">{c.content}</span>
            <span className="row__sub">
              {c.doctor.name} · {c.doctor.hospital}
            </span>
            <span className="row__sub">기한 {formatDate(c.dueDate)}</span>
          </Link>
          <div className="row__action">
            <button
              type="button"
              className="btn btn--small"
              onClick={() => onComplete(c.id)}
              disabled={complete.isPending && complete.variables === c.id}
            >
              완료
            </button>
          </div>
        </li>
      ))}
    </ul>
  )
}

function UpcomingRow({ item }: { item: UpcomingDoctor }) {
  return (
    <li className="row">
      <Link className="row__main" to={`/doctors/${item.doctor.id}`}>
        {item.pastDue && (
          <span className="row__badges">
            <Badge tone={PAST_DUE_BADGE.tone}>{PAST_DUE_BADGE.label}</Badge>
          </span>
        )}
        <span className="row__title">{item.doctor.name}</span>
        <span className="row__sub">
          {item.doctor.hospital} · {item.department}
        </span>
        <span className="row__sub">방문 예정 {formatDate(item.nextVisitDate)}</span>
        <span className="chips">
          <span className={item.readyAnswerCount === 0 ? 'chip chip--zero' : 'chip'}>
            전달할 답변 {item.readyAnswerCount}
          </span>
          <span className={item.openCommitmentCount === 0 ? 'chip chip--zero' : 'chip'}>
            남은 약속 {item.openCommitmentCount}
          </span>
        </span>
      </Link>
    </li>
  )
}

function ResetButton() {
  const toast = useToast()
  const reset = useResetMutation()

  const onReset = () => {
    if (!window.confirm('모든 데이터를 지우고 가상 데이터로 다시 채웁니다. 계속할까요?')) return
    reset.mutate(undefined, {
      onSuccess: () => toast.show('가상 데이터로 초기화했습니다.'),
      onError: (e) => toast.show(errorMessage(e), 'danger'),
    })
  }

  return (
    <button type="button" className="btn btn--text btn--small" onClick={onReset} disabled={reset.isPending}>
      가상 데이터로 초기화
    </button>
  )
}
