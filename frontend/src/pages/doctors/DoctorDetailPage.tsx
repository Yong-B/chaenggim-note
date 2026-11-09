import { useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate, useParams } from 'react-router-dom'
import type { CommitmentResponse, DoctorDetailResponse, QuestionResponse, VisitResponse } from '../../api/types'
import { Badge } from '../../components/Badge'
import { EmptyState } from '../../components/EmptyState'
import { PageTitle } from '../../components/PageTitle'
import { QueryState } from '../../components/QueryState'
import { useToast } from '../../components/Toast'
import { formatDate, formatDateTime } from '../../lib/date'
import { errorMessage } from '../../lib/errors'
import { commitmentBadge, sourceLabel, statusLabel, statusTone } from '../../lib/labels'
import {
  useCompleteCommitmentMutation,
  useDeleteCommitmentMutation,
  useReopenCommitmentMutation,
} from '../../queries/commitments'
import { removeDoctorQuery, useDeleteDoctorMutation, useDoctorQuery } from '../../queries/doctors'
import { useDeleteVisitMutation } from '../../queries/visits'

/** 06 화면 명세 4절 — 의료진 상세. 화면 제목 = 의료진 이름. */
export function DoctorDetailPage() {
  const id = Number(useParams().id)
  const query = useDoctorQuery(id)

  return (
    <QueryState query={query}>{(detail) => <DoctorDetail detail={detail} />}</QueryState>
  )
}

function DoctorDetail({ detail }: { detail: DoctorDetailResponse }) {
  const { doctor, prep } = detail

  return (
    <>
      <PageTitle title={doctor.name} />

      {/* ① 기본 정보 */}
      <section className="section doctor-info" aria-label="기본 정보">
        <p className="row__text">
          {doctor.hospital} · {doctor.department}
        </p>
        {doctor.memo && <p className="memo muted-text">{doctor.memo}</p>}
        <p className="row__sub">
          {doctor.nextVisitDate ? `다음 방문 예정 ${formatDate(doctor.nextVisitDate)}` : '다음 방문 예정일 없음'}
        </p>
        <div className="inline-actions doctor-info__actions">
          <Link to={`/doctors/${doctor.id}/edit`}>정보 수정</Link>
          <DeleteDoctorButton id={doctor.id} />
        </div>
      </section>

      {/* ② 큰 버튼 */}
      <Link className="btn btn--primary btn--large btn--block-mobile doctor-visit-cta" to={`/doctors/${doctor.id}/visits/new`}>
        + 방문 기록
      </Link>

      {/* ③ 방문 전 준비 (시그니처) */}
      <section className="section section--prep" aria-labelledby="prep-title">
        <h2 className="section__title" id="prep-title">
          방문 전 준비
        </h2>

        <div className="prep-block">
          <h3 className="section__subtitle">전달할 답변</h3>
          {prep.readyQuestions.length === 0 ? (
            <EmptyState>전달할 답변이 없습니다.</EmptyState>
          ) : (
            <ul className="list">
              {prep.readyQuestions.map((q) => (
                <ReadyAnswerRow key={q.id} question={q} />
              ))}
            </ul>
          )}
        </div>

        <div className="prep-block">
          <h3 className="section__subtitle">확인이 필요한 질문</h3>
          {prep.openQuestions.length === 0 ? (
            <EmptyState>확인이 필요한 질문이 없습니다.</EmptyState>
          ) : (
            <ul className="list">
              {prep.openQuestions.map((q) => (
                <li key={q.id} className="row">
                  <div className="row__main">
                    <span className="row__badges">
                      <Badge tone={statusTone[q.status]}>{statusLabel[q.status]}</Badge>
                    </span>
                    <span className="row__text">{q.content}</span>
                    <span className="row__sub">받은 날 {formatDate(q.visitDate)}</span>
                  </div>
                  <div className="row__action">
                    <Link to={`/questions/${q.id}`}>상세</Link>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="prep-block">
          <h3 className="section__subtitle">남은 약속</h3>
          {prep.openCommitments.length === 0 ? (
            <EmptyState>남은 약속이 없습니다.</EmptyState>
          ) : (
            <ul className="list">
              {prep.openCommitments.map((c) => (
                <OpenCommitmentRow key={c.id} commitment={c} />
              ))}
            </ul>
          )}
        </div>

        <div className="prep-block">
          <h3 className="section__subtitle">지난 방문 메모</h3>
          {prep.latestVisit === null ? (
            <EmptyState>아직 방문 기록이 없습니다.</EmptyState>
          ) : (
            <div className="list">
              <div className="row">
                <div className="row__main">
                  <span className="row__sub">{formatDate(prep.latestVisit.visitDate)}</span>
                  <span className="row__text memo">{prep.latestVisit.memo ?? '메모 없음'}</span>
                </div>
              </div>
            </div>
          )}
        </div>
      </section>

      {/* ④ 방문 기록 */}
      <section className="section" aria-labelledby="visits-title">
        <h2 className="section__title" id="visits-title">
          방문 기록
        </h2>
        {detail.visits.length === 0 ? (
          <EmptyState>아직 방문 기록이 없습니다.</EmptyState>
        ) : (
          <ul className="list">
            {detail.visits.map((v) => (
              <VisitRow key={v.id} visit={v} />
            ))}
          </ul>
        )}
      </section>

      {/* ⑤ 전달 완료한 질문 (비면 숨김) */}
      {detail.deliveredQuestions.length > 0 && (
        <section className="section" aria-labelledby="delivered-title">
          <h2 className="section__title" id="delivered-title">
            전달 완료한 질문
          </h2>
          <ul className="list">
            {detail.deliveredQuestions.map((q) => (
              <li key={q.id} className="row">
                <div className="row__main">
                  <span className="row__text">{q.content}</span>
                  {q.deliveredAt && <span className="row__sub">전달 {formatDateTime(q.deliveredAt)}</span>}
                </div>
                <div className="row__action">
                  <Link to={`/questions/${q.id}`}>상세</Link>
                </div>
              </li>
            ))}
          </ul>
        </section>
      )}

      {/* ⑥ 완료한 약속 (비면 숨김) */}
      {detail.doneCommitments.length > 0 && (
        <section className="section" aria-labelledby="done-title">
          <h2 className="section__title" id="done-title">
            완료한 약속
          </h2>
          <ul className="list">
            {detail.doneCommitments.map((c) => (
              <DoneCommitmentRow key={c.id} commitment={c} />
            ))}
          </ul>
        </section>
      )}
    </>
  )
}

function DeleteDoctorButton({ id }: { id: number }) {
  const navigate = useNavigate()
  const toast = useToast()
  const queryClient = useQueryClient()
  const mutation = useDeleteDoctorMutation(id)

  const onDelete = async () => {
    if (!window.confirm('이 의료진을 목록에서 삭제할까요? 기록은 보관되지만 다시 볼 수 없습니다.')) return
    try {
      await mutation.mutateAsync()
      navigate('/doctors')
      removeDoctorQuery(queryClient, id)
      toast.show('의료진을 삭제했습니다.')
    } catch (e) {
      toast.show(errorMessage(e), 'danger')
    }
  }

  return (
    <button type="button" className="btn btn--text btn--danger" onClick={onDelete} disabled={mutation.isPending}>
      의료진 삭제
    </button>
  )
}

function ReadyAnswerRow({ question: q }: { question: QuestionResponse }) {
  const source = q.sourceType ? sourceLabel[q.sourceType] : ''
  return (
    <li className="row">
      <div className="row__main">
        <span className="row__title">{q.content}</span>
        {q.answer && <span className="row__text memo">{q.answer}</span>}
        <span className="row__sub">출처: {q.sourceDetail ? `${source} · ${q.sourceDetail}` : source}</span>
      </div>
      <div className="row__action">
        <Link to={`/questions/${q.id}`}>상세</Link>
      </div>
    </li>
  )
}

function OpenCommitmentRow({ commitment: c }: { commitment: CommitmentResponse }) {
  const toast = useToast()
  const complete = useCompleteCommitmentMutation()
  const badge = commitmentBadge(c)

  const onComplete = () =>
    complete.mutate(c.id, {
      onSuccess: () => toast.show('약속을 완료했습니다.'),
      onError: (e) => toast.show(errorMessage(e), 'danger'),
    })

  return (
    <li className="row">
      <div className="row__main">
        {badge && (
          <span className="row__badges">
            <Badge tone={badge.tone}>{badge.label}</Badge>
          </span>
        )}
        <span className="row__text">{c.content}</span>
        <span className="row__sub">기한 {formatDate(c.dueDate)}</span>
      </div>
      <div className="row__action">
        <button type="button" className="btn btn--small" onClick={onComplete} disabled={complete.isPending}>
          완료
        </button>
        <Link to={`/commitments/${c.id}/edit`}>수정</Link>
      </div>
    </li>
  )
}

function VisitRow({ visit: v }: { visit: VisitResponse }) {
  const toast = useToast()
  const mutation = useDeleteVisitMutation()

  const onDelete = () => {
    if (!window.confirm('이 방문 기록과 그때 기록한 질문·약속을 모두 삭제할까요?')) return
    mutation.mutate(
      { id: v.id, doctorId: v.doctorId },
      {
        onSuccess: () => toast.show('방문 기록을 삭제했습니다.'),
        onError: (e) => toast.show(errorMessage(e), 'danger'),
      },
    )
  }

  return (
    <li className="row row--top">
      <div className="row__main">
        <span className="row__title">{formatDate(v.visitDate)}</span>
        {v.memo && <span className="row__text memo">{v.memo}</span>}
        <span className="row__sub">
          질문 {v.questionCount} · 약속 {v.commitmentCount}
        </span>
      </div>
      <div className="row__action">
        <Link to={`/visits/${v.id}/edit`}>수정</Link>
        {v.hasDelivered ? (
          <span className="muted-text row__note">전달 완료 질문이 있어 삭제 불가</span>
        ) : (
          <button type="button" className="btn btn--text btn--danger" onClick={onDelete} disabled={mutation.isPending}>
            삭제
          </button>
        )}
      </div>
    </li>
  )
}

function DoneCommitmentRow({ commitment: c }: { commitment: CommitmentResponse }) {
  const toast = useToast()
  const reopen = useReopenCommitmentMutation()
  const remove = useDeleteCommitmentMutation()

  const onReopen = () =>
    reopen.mutate(c.id, {
      onSuccess: () => toast.show('완료를 취소했습니다.'),
      onError: (e) => toast.show(errorMessage(e), 'danger'),
    })

  const onDelete = () => {
    if (!window.confirm('이 약속을 삭제할까요?')) return
    remove.mutate(
      { id: c.id, doctorId: c.doctor.id },
      {
        onSuccess: () => toast.show('약속을 삭제했습니다.'),
        onError: (e) => toast.show(errorMessage(e), 'danger'),
      },
    )
  }

  return (
    <li className="row row--done">
      <div className="row__main">
        <span className="row__title">{c.content}</span>
        {c.doneAt && <span className="row__sub">완료 {formatDateTime(c.doneAt)}</span>}
      </div>
      <div className="row__action">
        <button type="button" className="btn btn--small" onClick={onReopen} disabled={reopen.isPending}>
          완료 취소
        </button>
        <button type="button" className="btn btn--text btn--danger" onClick={onDelete} disabled={remove.isPending}>
          삭제
        </button>
      </div>
    </li>
  )
}
