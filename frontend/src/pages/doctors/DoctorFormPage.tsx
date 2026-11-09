import { PageTitle } from '../../components/PageTitle'

/** 등록(/doctors/new)·수정(/doctors/:id/edit) 공용. */
export function DoctorFormPage({ mode }: { mode: 'create' | 'edit' }) {
  return <PageTitle title={mode === 'create' ? '의료진 등록' : '의료진 정보 수정'} />
}
