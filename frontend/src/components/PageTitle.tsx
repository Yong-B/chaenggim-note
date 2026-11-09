import { useEffect } from 'react'

/** 화면 제목 + document.title = "{화면 제목} · 챙김노트" (06 문서 0.2절). */
export function PageTitle({ title }: { title: string }) {
  useEffect(() => {
    document.title = `${title} · 챙김노트`
  }, [title])

  return <h1 className="page-title">{title}</h1>
}
