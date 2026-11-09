import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../../api/client'
import type { DoctorRequest, DoctorResponse } from '../../api/types'
import { Field } from '../../components/Field'
import { PageTitle } from '../../components/PageTitle'
import { QueryState } from '../../components/QueryState'
import { useToast } from '../../components/Toast'
import { errorMessage } from '../../lib/errors'
import { clearError, fieldDescribedBy, splitFieldErrors } from '../../lib/fieldErrors'
import { useCreateDoctorMutation, useDoctorQuery, useUpdateDoctorMutation } from '../../queries/doctors'

type Values = { name: string; hospital: string; department: string; memo: string; nextVisitDate: string }

const FIELDS = ['name', 'hospital', 'department', 'memo', 'nextVisitDate'] as const

const EMPTY: Values = { name: '', hospital: '', department: '', memo: '', nextVisitDate: '' }

/** 06 화면 명세 3절 — 의료진 등록(/doctors/new)·수정(/doctors/:id/edit). */
export function DoctorFormPage({ mode }: { mode: 'create' | 'edit' }) {
  return mode === 'create' ? <CreateDoctor /> : <EditDoctor />
}

function CreateDoctor() {
  const navigate = useNavigate()
  const toast = useToast()
  const mutation = useCreateDoctorMutation()

  return (
    <>
      <PageTitle title="의료진 등록" />
      <DoctorForm
        initial={EMPTY}
        cancelTo="/doctors"
        pending={mutation.isPending}
        onSubmit={async (body) => {
          const doctor = await mutation.mutateAsync(body)
          navigate(`/doctors/${doctor.id}`)
          toast.show('의료진을 등록했습니다.')
        }}
      />
    </>
  )
}

function EditDoctor() {
  const id = Number(useParams().id)
  const query = useDoctorQuery(id)

  return (
    <>
      <PageTitle title="의료진 정보 수정" />
      <QueryState query={query}>{(detail) => <EditDoctorForm doctor={detail.doctor} />}</QueryState>
    </>
  )
}

function EditDoctorForm({ doctor }: { doctor: DoctorResponse }) {
  const navigate = useNavigate()
  const toast = useToast()
  const mutation = useUpdateDoctorMutation(doctor.id)

  return (
    <DoctorForm
      initial={{
        name: doctor.name,
        hospital: doctor.hospital,
        department: doctor.department,
        memo: doctor.memo ?? '',
        nextVisitDate: doctor.nextVisitDate ?? '',
      }}
      cancelTo={`/doctors/${doctor.id}`}
      pending={mutation.isPending}
      onSubmit={async (body) => {
        await mutation.mutateAsync(body)
        navigate(`/doctors/${doctor.id}`)
        toast.show('의료진 정보를 수정했습니다.')
      }}
    />
  )
}

function DoctorForm({
  initial,
  cancelTo,
  pending,
  onSubmit,
}: {
  initial: Values
  cancelTo: string
  pending: boolean
  onSubmit: (body: DoctorRequest) => Promise<void>
}) {
  const toast = useToast()
  const [values, setValues] = useState<Values>(initial)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [formErrors, setFormErrors] = useState<string[]>([])

  const set = (field: keyof Values) => (value: string) => {
    setValues((v) => ({ ...v, [field]: value }))
    setErrors((prev) => clearError(prev, field))
  }

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    setFormErrors([])
    try {
      await onSubmit({
        name: values.name,
        hospital: values.hospital,
        department: values.department,
        memo: values.memo.trim() === '' ? null : values.memo,
        nextVisitDate: values.nextVisitDate === '' ? null : values.nextVisitDate,
      })
    } catch (err) {
      if (err instanceof ApiError && err.status === 400) {
        const split = splitFieldErrors(err.fieldErrors, (f) => (FIELDS as readonly string[]).includes(f))
        setErrors(split.errors)
        setFormErrors(split.formErrors)
      } else {
        toast.show(errorMessage(err), 'danger')
      }
    }
  }

  const input = (field: keyof Values) => ({
    id: field,
    value: values[field],
    'aria-invalid': !!errors[field],
    'aria-describedby': fieldDescribedBy(field, errors[field]),
  })

  return (
    <form className="form" onSubmit={submit} noValidate>
      {formErrors.length > 0 && (
        <div className="form__error" role="alert">
          {formErrors.map((m) => (
            <p key={m}>{m}</p>
          ))}
        </div>
      )}
      <Field label="이름" htmlFor="name" required error={errors.name}>
        <input {...input('name')} type="text" required onChange={(e) => set('name')(e.target.value)} />
      </Field>
      <Field label="병원" htmlFor="hospital" required error={errors.hospital}>
        <input {...input('hospital')} type="text" required onChange={(e) => set('hospital')(e.target.value)} />
      </Field>
      <Field label="진료과" htmlFor="department" required error={errors.department}>
        <input {...input('department')} type="text" required onChange={(e) => set('department')(e.target.value)} />
      </Field>
      <Field label="메모" htmlFor="memo" error={errors.memo}>
        <textarea {...input('memo')} placeholder="예: 오전 진료 후 방문 선호" onChange={(e) => set('memo')(e.target.value)} />
      </Field>
      <Field label="다음 방문 예정일" htmlFor="nextVisitDate" error={errors.nextVisitDate}>
        <input {...input('nextVisitDate')} type="date" onChange={(e) => set('nextVisitDate')(e.target.value)} />
      </Field>
      <div className="form__actions">
        <button type="submit" className="btn btn--primary" disabled={pending}>
          {pending ? '저장 중…' : '저장'}
        </button>
        <Link to={cancelTo}>취소</Link>
      </div>
    </form>
  )
}
