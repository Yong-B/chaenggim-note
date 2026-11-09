import { PageTitle } from '../components/PageTitle'
import { useHomeQuery } from '../queries/home'

/** T6 임시 홈: /api/home 응답을 그대로 보여 준다. 화면 구성은 T8. */
export function HomePage() {
  const { data, error, isPending } = useHomeQuery()

  return (
    <>
      <PageTitle title="오늘 챙길 것" />
      {isPending && <p>불러오는 중…</p>}
      {error && <p>데이터를 불러오지 못했습니다.</p>}
      {data && <pre data-testid="home-json">{JSON.stringify(data, null, 2)}</pre>}
    </>
  )
}
