import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../../api/client'
import type { VisitDetailResponse } from '../../api/types'
import { Field } from '../../components/Field'
import { PageTitle } from '../../components/PageTitle'
import { QueryState } from '../../components/QueryState'
import { useToast } from '../../components/Toast'
import { errorMessage } from '../../lib/errors'
import { clearError, fieldDescribedBy, splitFieldErrors } from '../../lib/fieldErrors'
import { useUpdateVisitMutation, useVisitQuery } from '../../queries/visits'

/** 06 화면 명세 6절 — 방문 기록 수정 (날짜·메모만, D15). */
export function VisitEditPage() {
  const id = Number(useParams().id)
  const query = useVisitQuery(id)

  return <QueryState query={query}>{(visit) => <VisitEditForm visit={visit} />}</QueryState>
}

function VisitEditForm({ visit }: { visit: VisitDetailResponse }) {
  const navigate = useNavigate()
  const toast = useToast()
  const mutation = useUpdateVisitMutation(visit.id)
  const [visitDate, setVisitDate] = useState(visit.visitDate)
  const [memo, setMemo] = useState(visit.memo ?? '')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [formErrors, setFormErrors] = useState<string[]>([])

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    setFormErrors([])
    try {
      await mutation.mutateAsync({ visitDate, memo: memo.trim() === '' ? null : memo })
      navigate(`/doctors/${visit.doctorId}`)
      toast.show('방문 기록을 수정했습니다.')
    } catch (err) {
      if (err instanceof ApiError && err.status === 400) {
        const split = splitFieldErrors(err.fieldErrors, (f) => f === 'visitDate' || f === 'memo')
        setErrors(split.errors)
        setFormErrors(split.formErrors)
      } else {
        toast.show(errorMessage(err), 'danger')
      }
    }
  }

  return (
    <>
      <PageTitle title="방문 기록 수정" lead={`${visit.doctor.name} · ${visit.doctor.hospital}`} />
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
              setErrors((prev) => clearError(prev, 'visitDate'))
            }}
          />
        </Field>
        <Field label="방문 메모" htmlFor="memo" error={errors.memo}>
          <textarea
            id="memo"
            value={memo}
            aria-invalid={!!errors.memo}
            aria-describedby={fieldDescribedBy('memo', errors.memo)}
            onChange={(e) => {
              setMemo(e.target.value)
              setErrors((prev) => clearError(prev, 'memo'))
            }}
          />
        </Field>
        <p className="field__hint">질문과 약속은 각각의 화면에서 수정할 수 있습니다.</p>
        <div className="form__actions">
          <button type="submit" className="btn btn--primary" disabled={mutation.isPending}>
            {mutation.isPending ? '저장 중…' : '저장'}
          </button>
          <Link to={`/doctors/${visit.doctorId}`}>취소</Link>
        </div>
      </form>
    </>
  )
}
