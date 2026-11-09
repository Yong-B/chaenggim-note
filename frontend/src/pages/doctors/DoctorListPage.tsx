import { useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { EmptyState } from '../../components/EmptyState'
import { PageTitle } from '../../components/PageTitle'
import { QueryState } from '../../components/QueryState'
import { formatDate } from '../../lib/date'
import { useDoctorsQuery } from '../../queries/doctors'

/** 06 화면 명세 2절 — 의료진 목록. 검색어는 URL ?q= 에 둔다(뒤로 가기로 이전 검색 복귀). */
export function DoctorListPage() {
  const [params, setParams] = useSearchParams()
  const q = (params.get('q') ?? '').trim()
  const query = useDoctorsQuery(q)

  return (
    <>
      <PageTitle title="의료진" />
      <div className="toolbar">
        {/* key로 URL이 바뀔 때(뒤로 가기) 입력칸을 새 검색어로 맞춘다 */}
        <SearchForm key={q} initial={q} onSearch={(value) => setParams(value ? { q: value } : {})} />
        <Link className="btn btn--primary" to="/doctors/new">
          + 의료진 등록
        </Link>
      </div>
      <QueryState query={query}>
        {(doctors) =>
          doctors.length === 0 ? (
            <EmptyState>{q ? `'${q}'에 해당하는 의료진이 없습니다.` : '등록된 의료진이 없습니다.'}</EmptyState>
          ) : (
            <ul className="list">
              {doctors.map((d) => (
                <li key={d.id} className="row">
                  <Link className="row__main" to={`/doctors/${d.id}`}>
                    <span className="row__title">{d.name}</span>
                    <span className="row__sub">
                      {d.hospital} · {d.department}
                    </span>
                    {d.nextVisitDate && <span className="row__sub">방문 예정 {formatDate(d.nextVisitDate)}</span>}
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

function SearchForm({ initial, onSearch }: { initial: string; onSearch: (q: string) => void }) {
  const [value, setValue] = useState(initial)

  const onSubmit = (e: FormEvent) => {
    e.preventDefault()
    onSearch(value.trim())
  }

  return (
    <form className="search" role="search" onSubmit={onSubmit}>
      <label className="visually-hidden" htmlFor="doctor-search">
        이름 또는 병원으로 검색
      </label>
      <input
        id="doctor-search"
        className="input"
        type="search"
        placeholder="이름 또는 병원으로 검색"
        value={value}
        onChange={(e) => setValue(e.target.value)}
      />
      <button type="submit" className="btn">
        검색
      </button>
    </form>
  )
}
