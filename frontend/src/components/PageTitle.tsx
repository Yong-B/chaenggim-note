import { useEffect, type ReactNode } from 'react'

/**
 * 화면 제목 + document.title = "{화면 제목} · 챙김노트" (06 문서 0.2절).
 * 휴대폰은 앱 이름 + 화면 제목, PC는 화면 제목만(앱 이름은 왼쪽 메뉴에).
 */
export function PageTitle({ title, lead }: { title: string; lead?: ReactNode }) {
  useEffect(() => {
    document.title = `${title} · 챙김노트`
  }, [title])

  return (
    <header className="page-header">
      <span className="app-name">챙김노트</span>
      <h1 className="page-title">{title}</h1>
      {lead && <p className="page-header__lead">{lead}</p>}
    </header>
  )
}
