import { Link, Outlet, useLocation } from 'react-router-dom'

type MenuKey = 'home' | 'doctors' | 'questions'

const MENU: { key: MenuKey; to: string; label: string }[] = [
  { key: 'home', to: '/', label: '홈' },
  { key: 'doctors', to: '/doctors', label: '의료진' },
  { key: 'questions', to: '/questions', label: '질문함' },
]

/** 06 문서 0.1절 "메뉴 활성" 열: 방문·약속 화면도 의료진 메뉴. 찾을 수 없음은 없음. */
function activeMenu(pathname: string): MenuKey | null {
  if (pathname === '/') return 'home'
  if (/^\/(doctors|visits|commitments)(\/|$)/.test(pathname)) return 'doctors'
  if (/^\/questions(\/|$)/.test(pathname)) return 'questions'
  return null
}

/** 메뉴 3개 + 본문. 반응형(하단 탭 ↔ 왼쪽 메뉴)과 스타일은 T7에서. */
export function Layout() {
  const active = activeMenu(useLocation().pathname)

  return (
    <div className="app">
      <nav className="app-nav" aria-label="주 메뉴">
        <span className="app-name">챙김노트</span>
        {MENU.map((m) => (
          <Link
            key={m.key}
            to={m.to}
            className={m.key === active ? 'app-nav__link app-nav__link--active' : 'app-nav__link'}
            aria-current={m.key === active ? 'page' : undefined}
          >
            {m.label}
          </Link>
        ))}
      </nav>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}
