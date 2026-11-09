import { useQueryClient } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../../api/client'
import type { CommitmentResponse } from '../../api/types'
import { Field } from '../../components/Field'
import { PageTitle } from '../../components/PageTitle'
import { QueryState } from '../../components/QueryState'
import { useToast } from '../../components/Toast'
import { errorMessage } from '../../lib/errors'
import { clearError, fieldDescribedBy, splitFieldErrors } from '../../lib/fieldErrors'
import {
  removeCommitmentQuery,
  useCommitmentQuery,
  useDeleteCommitmentMutation,
  useReopenCommitmentMutation,
  useUpdateCommitmentMutation,
} from '../../queries/commitments'

/** 06 화면 명세 9절 — 약속 수정. */
export function CommitmentEditPage() {
  const id = Number(useParams().id)
  const query = useCommitmentQuery(id)

  return (
    <QueryState query={query}>
      {(c) => (
        <>
          <PageTitle title="약속 수정" lead={`${c.doctor.name} · ${c.doctor.hospital}`} />
          {c.done ? <DoneNotice commitment={c} /> : <CommitmentForm key={c.id} commitment={c} />}
        </>
      )}
    </QueryState>
  )
}

/** R4: 완료된 약속은 완료를 취소한 뒤 수정. */
function DoneNotice({ commitment: c }: { commitment: CommitmentResponse }) {
  const toast = useToast()
  const reopen = useReopenCommitmentMutation()

  const onReopen = () =>
    reopen.mutate(c.id, {
      onSuccess: () => toast.show('완료를 취소했습니다.'),
      onError: (e) => toast.show(errorMessage(e), 'danger'),
    })

  return (
    <div className="form">
      <p className="notice">완료한 약속은 완료를 취소한 뒤 수정할 수 있습니다.</p>
      <div className="form__actions">
        <button type="button" className="btn" onClick={onReopen} disabled={reopen.isPending}>
          완료 취소
        </button>
        <Link to={`/doctors/${c.doctor.id}`}>취소</Link>
      </div>
    </div>
  )
}

function CommitmentForm({ commitment: c }: { commitment: CommitmentResponse }) {
  const navigate = useNavigate()
  const toast = useToast()
  const queryClient = useQueryClient()
  const update = useUpdateCommitmentMutation(c.id)
  const remove = useDeleteCommitmentMutation()
  const [content, setContent] = useState(c.content)
  const [dueDate, setDueDate] = useState(c.dueDate)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [formErrors, setFormErrors] = useState<string[]>([])
  const doctorPath = `/doctors/${c.doctor.id}`

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    setFormErrors([])
    try {
      await update.mutateAsync({ content, dueDate: dueDate === '' ? null : dueDate })
      navigate(doctorPath)
      toast.show('약속을 수정했습니다.')
    } catch (err) {
      if (err instanceof ApiError && err.status === 400) {
        const split = splitFieldErrors(err.fieldErrors, (f) => f === 'content' || f === 'dueDate')
        setErrors(split.errors)
        setFormErrors(split.formErrors)
      } else {
        toast.show(errorMessage(err), 'danger')
      }
    }
  }

  const onDelete = async () => {
    if (!window.confirm('이 약속을 삭제할까요?')) return
    try {
      await remove.mutateAsync({ id: c.id, doctorId: c.doctor.id })
      navigate(doctorPath)
      removeCommitmentQuery(queryClient, c.id)
      toast.show('약속을 삭제했습니다.')
    } catch (err) {
      toast.show(errorMessage(err), 'danger')
    }
  }

  return (
    <form className="form" onSubmit={submit} noValidate>
      {formErrors.length > 0 && (
        <div className="form__error" role="alert">
          {formErrors.map((m) => (
            <p key={m}>{m}</p>
          ))}
        </div>
      )}
      <Field label="약속 내용" htmlFor="content" required error={errors.content}>
        <input
          id="content"
          type="text"
          required
          value={content}
          aria-invalid={!!errors.content}
          aria-describedby={fieldDescribedBy('content', errors.content)}
          onChange={(e) => {
            setContent(e.target.value)
            setErrors((prev) => clearError(prev, 'content'))
          }}
        />
      </Field>
      <Field
        label="기한"
        htmlFor="dueDate"
        required
        error={errors.dueDate}
        hint="기한을 바꿀 때는 오늘 이후 날짜만 선택할 수 있습니다."
      >
        <input
          id="dueDate"
          type="date"
          required
          value={dueDate}
          aria-invalid={!!errors.dueDate}
          aria-describedby={fieldDescribedBy('dueDate', errors.dueDate, true)}
          onChange={(e) => {
            setDueDate(e.target.value)
            setErrors((prev) => clearError(prev, 'dueDate'))
          }}
        />
      </Field>
      <div className="form__actions">
        <button type="submit" className="btn btn--primary" disabled={update.isPending}>
          {update.isPending ? '저장 중…' : '저장'}
        </button>
        <Link to={doctorPath}>취소</Link>
        <button
          type="button"
          className="btn btn--text btn--danger form__actions-end"
          onClick={onDelete}
          disabled={remove.isPending}
        >
          약속 삭제
        </button>
      </div>
    </form>
  )
}
