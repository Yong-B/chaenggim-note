import { Link } from 'react-router-dom'
import { PageTitle } from '../components/PageTitle'

export function NotFoundPage() {
  return (
    <>
      <PageTitle title="찾을 수 없음" />
      <p>요청한 정보를 찾을 수 없습니다.</p>
      <Link to="/">홈으로</Link>
    </>
  )
}
