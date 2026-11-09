import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../../api/client'
import type { DoctorResponse, VisitCreateRequest } from '../../api/types'
import { Field } from '../../components/Field'
import { PageTitle } from '../../components/PageTitle'
import { QueryState } from '../../components/QueryState'
import { useToast } from '../../components/Toast'
import { todayString } from '../../lib/date'
import { errorMessage } from '../../lib/errors'
import { clearError, fieldDescribedBy, toFieldErrorMap } from '../../lib/fieldErrors'
import { useDoctorQuery } from '../../queries/doctors'
import { useCreateVisitMutation } from '../../queries/visits'

const MAX_ROWS = 10

type QuestionRow = { key: string; content: string }
type CommitmentRow = { key: string; content: string; dueDate: string }

const newQuestion = (): QuestionRow => ({ key: crypto.randomUUID(), content: '' })
const newCommitment = (): CommitmentRow => ({ key: crypto.randomUUID(), content: '', dueDate: '' })

/** 06 화면 명세 5절 — 방문 기록 작성. */
export function VisitCreatePage() {
  const doctorId = Number(useParams().id)
  const query = useDoctorQuery(doctorId)

  return (
    <QueryState query={query}>{(detail) => <VisitCreateForm doctor={detail.doctor} />}</QueryState>
  )
}

function VisitCreateForm({ doctor }: { doctor: DoctorResponse }) {
  const navigate = useNavigate()
  const toast = useToast()
  const mutation = useCreateVisitMutation(doctor.id)

  const [visitDate, setVisitDate] = useState(todayString())
  const [memo, setMemo] = useState('')
  const [nextVisitDate, setNextVisitDate] = useState('')
  const [questions, setQuestions] = useState<QuestionRow[]>([newQuestion()])
  const [commitments, setCommitments] = useState<CommitmentRow[]>([newCommitment()])
  /** 필드 에러. 반복 행은 "q:{key}", "c:{key}:content", "c:{key}:dueDate" 키를 쓴다. */
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [formErrors, setFormErrors] = useState<string[]>([])

  const clear = (field: string) => setErrors((prev) => clearError(prev, field))

  const updateQuestion = (key: string, content: string) => {
    setQuestions((rows) => rows.map((r) => (r.key === key ? { ...r, content } : r)))
    clear(`q:${key}`)
  }
  const updateCommitment = (key: string, patch: Partial<CommitmentRow>) => {
    setCommitments((rows) => rows.map((r) => (r.key === key ? { ...r, ...patch } : r)))
    Object.keys(patch).forEach((f) => clear(`c:${key}:${f}`))
  }

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    setFormErrors([])

    // 07 문서 4.5절: 전송 직전 빈 행 제거 + 보낸 인덱스 → 화면 행 key 매핑
    const sentQuestions = questions.filter((q) => q.content.trim() !== '')
    const sentCommitments = commitments.filter((c) => c.content.trim() !== '' || c.dueDate !== '')
    const body: VisitCreateRequest = {
      visitDate,
      memo: memo.trim() === '' ? null : memo,
      nextVisitDate: nextVisitDate === '' ? null : nextVisitDate,
      questions: sentQuestions.map((q) => ({ content: q.content, productTag: null })),
      commitments: sentCommitments.map((c) => ({ content: c.content, dueDate: c.dueDate === '' ? null : c.dueDate })),
    }

    try {
      await mutation.mutateAsync(body)
      navigate(`/doctors/${doctor.id}`)
      toast.show('방문 기록을 저장했습니다.')
    } catch (err) {
      if (!(err instanceof ApiError && err.status === 400)) {
        toast.show(errorMessage(err), 'danger')
        return
      }
      const next: Record<string, string> = {}
      const unmatched: string[] = []
      for (const [field, message] of Object.entries(toFieldErrorMap(err.fieldErrors))) {
        const q = /^questions\[(\d+)\]\.content$/.exec(field)
        const c = /^commitments\[(\d+)\]\.(content|dueDate)$/.exec(field)
        if (field === 'visitDate' || field === 'memo' || field === 'nextVisitDate') next[field] = message
        else if (q && sentQuestions[Number(q[1])]) next[`q:${sentQuestions[Number(q[1])].key}`] = message
        else if (c && sentCommitments[Number(c[1])]) next[`c:${sentCommitments[Number(c[1])].key}:${c[2]}`] = message
        else unmatched.push(message)
      }
      setErrors(next)
      setFormErrors(unmatched)
    }
  }

  const pending = mutation.isPending

  return (
    <>
      <PageTitle title="방문 기록" lead={`${doctor.name} · ${doctor.hospital}`} />
      <form className="form" onSubmit={submit} noValidate>
        {formErrors.length > 0 && (
          <div className="form__error" role="alert">
            {formErrors.map((m) => (
              <p key={m}>{m}</p>
            ))}
          </div>
        )}

        <Field label="방문 날짜" htmlFor="visitDate" required error={errors.visitDate}>
          <input
            id="visitDate"
            type="date"
            required
            value={visitDate}
            aria-invalid={!!errors.visitDate}
            aria-describedby={fieldDescribedBy('visitDate', errors.visitDate)}
            onChange={(e) => {
              setVisitDate(e.target.value)
              clear('visitDate')
            }}
          />
        </Field>

        <Field label="방문 메모" htmlFor="memo" error={errors.memo}>
          <textarea
            id="memo"
            placeholder="예: 고령 환자 처방 경험 이야기 나눔"
            value={memo}
            aria-invalid={!!errors.memo}
            aria-describedby={fieldDescribedBy('memo', errors.memo)}
            onChange={(e) => {
              setMemo(e.target.value)
              clear('memo')
            }}
          />
        </Field>

        <fieldset className="form__group">
          <legend className="section__subtitle">받은 질문</legend>
          {questions.map((q, i) => {
            const id = `question-${q.key}`
            const error = errors[`q:${q.key}`]
            return (
              <div key={q.key} className="repeat-row">
                <div className="repeat-row__content">
                  <label className="visually-hidden" htmlFor={id}>
                    질문 {i + 1}
                  </label>
                  <input
                    id={id}
                    type="text"
                    placeholder={`질문 ${i + 1}`}
                    value={q.content}
                    aria-invalid={!!error}
                    aria-describedby={fieldDescribedBy(id, error)}
                    onChange={(e) => updateQuestion(q.key, e.target.value)}
                  />
                  {error && (
                    <span className="field__error" id={`${id}-error`}>
                      {error}
                    </span>
                  )}
                </div>
                {questions.length > 1 && (
                  <button
                    type="button"
                    className="btn btn--small"
                    aria-label={`질문 ${i + 1} 삭제`}
                    onClick={() => setQuestions((rows) => rows.filter((r) => r.key !== q.key))}
                  >
                    삭제
                  </button>
                )}
              </div>
            )
          })}
          <AddRow
            label="+ 질문 추가"
            limitMessage="질문은 한 번에 10개까지 입력할 수 있습니다."
            full={questions.length >= MAX_ROWS}
            onAdd={() => setQuestions((rows) => [...rows, newQuestion()])}
          />
        </fieldset>

        <fieldset className="form__group">
          <legend className="section__subtitle">약속한 것</legend>
          {commitments.map((c, i) => {
            const id = `commitment-${c.key}`
            const contentError = errors[`c:${c.key}:content`]
            const dueError = errors[`c:${c.key}:dueDate`]
            return (
              <div key={c.key} className="repeat-row">
                <div className="repeat-row__content">
                  <label className="visually-hidden" htmlFor={id}>
                    약속 {i + 1}
                  </label>
                  <input
                    id={id}
                    type="text"
                    placeholder={`약속 ${i + 1}`}
                    value={c.content}
                    aria-invalid={!!contentError}
                    aria-describedby={fieldDescribedBy(id, contentError)}
                    onChange={(e) => updateCommitment(c.key, { content: e.target.value })}
                  />
                  {contentError && (
                    <span className="field__error" id={`${id}-error`}>
                      {contentError}
                    </span>
                  )}
                </div>
                <div className="repeat-row__date">
                  <label className="field__label repeat-row__label" htmlFor={`${id}-due`}>
                    기한
                  </label>
                  <input
                    id={`${id}-due`}
                    type="date"
                    value={c.dueDate}
                    aria-invalid={!!dueError}
                    aria-describedby={fieldDescribedBy(`${id}-due`, dueError)}
                    onChange={(e) => updateCommitment(c.key, { dueDate: e.target.value })}
                  />
                  {dueError && (
                    <span className="field__error" id={`${id}-due-error`}>
                      {dueError}
                    </span>
                  )}
                </div>
                {commitments.length > 1 && (
                  <button
                    type="button"
                    className="btn btn--small repeat-row__remove"
                    aria-label={`약속 ${i + 1} 삭제`}
                    onClick={() => setCommitments((rows) => rows.filter((r) => r.key !== c.key))}
                  >
                    삭제
                  </button>
                )}
              </div>
            )
          })}
          <AddRow
            label="+ 약속 추가"
            limitMessage="약속은 한 번에 10개까지 입력할 수 있습니다."
            full={commitments.length >= MAX_ROWS}
            onAdd={() => setCommitments((rows) => [...rows, newCommitment()])}
          />
          <p className="field__hint">내용과 기한이 모두 빈 칸은 저장되지 않습니다.</p>
        </fieldset>

        <div className="form__group">
          <Field
            label="다음 방문 예정일"
            htmlFor="nextVisitDate"
            error={errors.nextVisitDate}
            hint="비워 두면 기존 예정일을 유지합니다."
          >
            <input
              id="nextVisitDate"
              type="date"
              value={nextVisitDate}
              aria-invalid={!!errors.nextVisitDate}
              aria-describedby={fieldDescribedBy('nextVisitDate', errors.nextVisitDate, true)}
              onChange={(e) => {
                setNextVisitDate(e.target.value)
                clear('nextVisitDate')
              }}
            />
          </Field>
        </div>

        <div className="form__actions">
          <button type="submit" className="btn btn--primary" disabled={pending}>
            {pending ? '저장 중…' : '저장'}
          </button>
          <Link to={`/doctors/${doctor.id}`}>취소</Link>
        </div>
      </form>
    </>
  )
}

function AddRow({
  label,
  limitMessage,
  full,
  onAdd,
}: {
  label: string
  limitMessage: string
  full: boolean
  onAdd: () => void
}) {
  return (
    <div className="repeat-add">
      <button type="button" className="btn btn--small" onClick={onAdd} disabled={full}>
        {label}
      </button>
      {full && <p className="field__hint">{limitMessage}</p>}
    </div>
  )
}
