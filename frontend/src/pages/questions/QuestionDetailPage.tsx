import { useQueryClient } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../../api/client'
import type { QuestionResponse, SourceType } from '../../api/types'
import { Badge } from '../../components/Badge'
import { Field } from '../../components/Field'
import { PageTitle } from '../../components/PageTitle'
import { ProductTagLabel } from '../../components/ProductTagLabel'
import { QueryState } from '../../components/QueryState'
import { useToast } from '../../components/Toast'
import { formatDate, formatDateTime } from '../../lib/date'
import { errorMessage } from '../../lib/errors'
import { clearError, fieldDescribedBy, splitFieldErrors } from '../../lib/fieldErrors'
import { sourceLabel, statusLabel, statusTone } from '../../lib/labels'
import {
  removeQuestionQuery,
  useAnswerMutation,
  useDeleteQuestionMutation,
  useDeliverMutation,
  useQuestionQuery,
  useStartCheckingMutation,
  useUpdateQuestionMutation,
} from '../../queries/questions'

const SOURCE_TYPES: SourceType[] = ['LABEL', 'CLINICAL', 'MEDICAL_TEAM']

/** 06 화면 명세 8절 — 질문 상세. */
export function QuestionDetailPage() {
  const id = Number(useParams().id)
  const query = useQuestionQuery(id)

  return (
    <>
      <PageTitle title="질문 상세" />
      <QueryState query={query}>{(q) => <QuestionDetail question={q} />}</QueryState>
    </>
  )
}

function QuestionDetail({ question: q }: { question: QuestionResponse }) {
  const delivered = q.status === 'DELIVERED'

  return (
    <>
      <QuestionCard question={q} />

      <section className="section" aria-label="답변">
        {q.status === 'UNANSWERED' && (
          <>
            <StartChecking id={q.id} />
            <AnswerForm question={q} />
          </>
        )}
        {q.status === 'CHECKING' && (
          <>
            <p className="notice">확인 중인 질문입니다. 확인되면 답변과 출처를 입력해 주세요.</p>
            <AnswerForm question={q} />
          </>
        )}
        {q.status === 'READY' && <ReadyArea question={q} />}
        {delivered && (
          <>
            <AnswerBox question={q} />
            {q.deliveredAt && <p className="row__sub answer-delivered">전달 {formatDateTime(q.deliveredAt)}</p>}
            <p className="notice">전달 완료된 질문은 수정할 수 없습니다.</p>
          </>
        )}
      </section>

      {!delivered && <DeleteQuestion id={q.id} doctorId={q.doctor.id} />}
    </>
  )
}

/** ① 질문 카드 + 인라인 편집 */
function QuestionCard({ question: q }: { question: QuestionResponse }) {
  const toast = useToast()
  const mutation = useUpdateQuestionMutation(q.id)
  const [editing, setEditing] = useState(false)
  const [content, setContent] = useState(q.content)
  const [error, setError] = useState<string | undefined>()

  const startEdit = () => {
    setContent(q.content)
    setError(undefined)
    setEditing(true)
  }

  const save = async (e: FormEvent) => {
    e.preventDefault()
    try {
      await mutation.mutateAsync(content)
      setEditing(false)
      toast.show('질문을 수정했습니다.')
    } catch (err) {
      if (err instanceof ApiError && err.status === 400) {
        setError(err.fieldErrors.find((f) => f.field === 'content')?.message ?? err.message)
      } else {
        toast.show(errorMessage(err), 'danger')
      }
    }
  }

  return (
    <section className="section question-card" aria-label="질문">
      <span className="row__badges">
        <Badge tone={statusTone[q.status]}>{statusLabel[q.status]}</Badge>
        <ProductTagLabel tag={q.productTag} />
      </span>
      {editing ? (
        <form onSubmit={save} noValidate>
          <div className="field">
            {/* 화면 명세에 없는 문구라 라벨은 화면에 보이지 않게 둔다 */}
            <label className="visually-hidden" htmlFor="question-content">
              질문
            </label>
            <textarea
              id="question-content"
              required
              value={content}
              aria-invalid={!!error}
              aria-describedby={fieldDescribedBy('question-content', error)}
              onChange={(e) => {
                setContent(e.target.value)
                setError(undefined)
              }}
            />
            {error && (
              <p className="field__error" id="question-content-error">
                {error}
              </p>
            )}
          </div>
          <div className="inline-actions">
            <button type="submit" className="btn btn--primary" disabled={mutation.isPending}>
              {mutation.isPending ? '저장 중…' : '저장'}
            </button>
            <button type="button" className="btn btn--text" onClick={() => setEditing(false)}>
              취소
            </button>
          </div>
        </form>
      ) : (
        <p className="question-card__content">{q.content}</p>
      )}
      <p className="row__text">
        <Link to={`/doctors/${q.doctor.id}`}>
          {q.doctor.name} · {q.doctor.hospital}
        </Link>
      </p>
      <p className="row__sub">받은 날 {formatDate(q.visitDate)}</p>
      {q.status !== 'DELIVERED' && !editing && (
        <div className="inline-actions">
          <button type="button" className="btn btn--text" onClick={startEdit}>
            질문 수정
          </button>
        </div>
      )}
    </section>
  )
}

function StartChecking({ id }: { id: number }) {
  const toast = useToast()
  const mutation = useStartCheckingMutation(id)

  const onClick = () =>
    mutation.mutate(undefined, {
      onSuccess: () => toast.show('확인 중으로 바꿨습니다.'),
      onError: (e) => toast.show(errorMessage(e), 'danger'),
    })

  return (
    <div className="start-checking">
      <button type="button" className="btn" onClick={onClick} disabled={mutation.isPending}>
        확인 시작
      </button>
      <p className="field__hint">학술팀 문의 등 확인을 시작하면 눌러 주세요.</p>
    </div>
  )
}

function ReadyArea({ question: q }: { question: QuestionResponse }) {
  const toast = useToast()
  const deliver = useDeliverMutation(q.id)
  const [open, setOpen] = useState(false)

  const onDeliver = () => {
    if (!window.confirm('의료진께 답변을 전달했나요? 전달 완료 후에는 수정할 수 없습니다.')) return
    deliver.mutate(undefined, {
      onSuccess: () => toast.show('전달 완료로 바꿨습니다.'),
      onError: (e) => toast.show(errorMessage(e), 'danger'),
    })
  }

  return (
    <>
      <AnswerBox question={q} />
      <div className="inline-actions answer-actions">
        <button type="button" className="btn btn--primary" onClick={onDeliver} disabled={deliver.isPending}>
          전달 완료
        </button>
      </div>
      <details className="disclosure" open={open} onToggle={(e) => setOpen(e.currentTarget.open)}>
        <summary>답변 수정</summary>
        {open && <AnswerForm question={q} onSaved={() => setOpen(false)} />}
      </details>
    </>
  )
}

function AnswerBox({ question: q }: { question: QuestionResponse }) {
  const source = q.sourceType ? sourceLabel[q.sourceType] : ''
  return (
    <div className="answer-box">
      <p className="answer-box__text">{q.answer}</p>
      <p className="answer-box__meta">출처: {q.sourceDetail ? `${source} · ${q.sourceDetail}` : source}</p>
      {q.answeredAt && <p className="answer-box__meta">답변 저장 {formatDateTime(q.answeredAt)}</p>}
    </div>
  )
}

/** 답변 폼: 답변*, 출처*(라디오 3개), 출처 상세. READY에서 열면 기존 값이 채워져 있다. */
function AnswerForm({ question: q, onSaved }: { question: QuestionResponse; onSaved?: () => void }) {
  const toast = useToast()
  const mutation = useAnswerMutation(q.id)
  const [answer, setAnswer] = useState(q.answer ?? '')
  const [sourceType, setSourceType] = useState<SourceType | null>(q.sourceType)
  const [sourceDetail, setSourceDetail] = useState(q.sourceDetail ?? '')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [formErrors, setFormErrors] = useState<string[]>([])

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    setFormErrors([])
    try {
      await mutation.mutateAsync({
        answer,
        sourceType,
        sourceDetail: sourceDetail.trim() === '' ? null : sourceDetail,
      })
      onSaved?.()
      toast.show('답변을 저장했습니다.')
    } catch (err) {
      if (err instanceof ApiError && err.status === 400) {
        const split = splitFieldErrors(err.fieldErrors, (f) => ['answer', 'sourceType', 'sourceDetail'].includes(f))
        setErrors(split.errors)
        setFormErrors(split.formErrors)
      } else {
        toast.show(errorMessage(err), 'danger')
      }
    }
  }

  return (
    <form className="form answer-form" onSubmit={submit} noValidate>
      {formErrors.length > 0 && (
        <div className="form__error" role="alert">
          {formErrors.map((m) => (
            <p key={m}>{m}</p>
          ))}
        </div>
      )}
      <Field label="답변" htmlFor="answer" required error={errors.answer}>
        <textarea
          id="answer"
          required
          value={answer}
          aria-invalid={!!errors.answer}
          aria-describedby={fieldDescribedBy('answer', errors.answer)}
          onChange={(e) => {
            setAnswer(e.target.value)
            setErrors((prev) => clearError(prev, 'answer'))
          }}
        />
      </Field>
      <fieldset
        className="field radio-field"
        aria-describedby={fieldDescribedBy('sourceType', errors.sourceType)}
      >
        <legend className="field__label">
          출처
          <span className="field__req" aria-hidden="true">
            *
          </span>
        </legend>
        <div className="radio-group">
          {SOURCE_TYPES.map((s) => (
            <label key={s} className="radio">
              <input
                type="radio"
                name="sourceType"
                value={s}
                required
                checked={sourceType === s}
                onChange={() => {
                  setSourceType(s)
                  setErrors((prev) => clearError(prev, 'sourceType'))
                }}
              />
              {sourceLabel[s]}
            </label>
          ))}
        </div>
        {errors.sourceType && (
          <p className="field__error" id="sourceType-error">
            {errors.sourceType}
          </p>
        )}
      </fieldset>
      <Field label="출처 상세" htmlFor="sourceDetail" error={errors.sourceDetail}>
        <input
          id="sourceDetail"
          type="text"
          placeholder="예: RACING 연구 요약 자료, 학술팀 메일 11/5"
          value={sourceDetail}
          aria-invalid={!!errors.sourceDetail}
          aria-describedby={fieldDescribedBy('sourceDetail', errors.sourceDetail)}
          onChange={(e) => {
            setSourceDetail(e.target.value)
            setErrors((prev) => clearError(prev, 'sourceDetail'))
          }}
        />
      </Field>
      <div className="form__actions">
        <button type="submit" className="btn btn--primary" disabled={mutation.isPending}>
          {mutation.isPending ? '저장 중…' : '답변 저장'}
        </button>
      </div>
    </form>
  )
}

function DeleteQuestion({ id, doctorId }: { id: number; doctorId: number }) {
  const navigate = useNavigate()
  const toast = useToast()
  const queryClient = useQueryClient()
  const mutation = useDeleteQuestionMutation(id, doctorId)

  const onDelete = async () => {
    if (!window.confirm('이 질문을 삭제할까요?')) return
    try {
      await mutation.mutateAsync()
      navigate(`/doctors/${doctorId}`)
      removeQuestionQuery(queryClient, id)
      toast.show('질문을 삭제했습니다.')
    } catch (e) {
      toast.show(errorMessage(e), 'danger')
    }
  }

  return (
    <div className="danger-zone">
      <button type="button" className="btn btn--text btn--danger" onClick={onDelete} disabled={mutation.isPending}>
        질문 삭제
      </button>
    </div>
  )
}
