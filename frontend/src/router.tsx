import { createBrowserRouter } from 'react-router-dom'
import { Layout } from './components/Layout'
import { ToastProvider } from './components/Toast'
import { HomePage } from './pages/HomePage'
import { NotFoundPage } from './pages/NotFoundPage'
import { CommitmentEditPage } from './pages/commitments/CommitmentEditPage'
import { DoctorDetailPage } from './pages/doctors/DoctorDetailPage'
import { DoctorFormPage } from './pages/doctors/DoctorFormPage'
import { DoctorListPage } from './pages/doctors/DoctorListPage'
import { QuestionDetailPage } from './pages/questions/QuestionDetailPage'
import { QuestionListPage } from './pages/questions/QuestionListPage'
import { VisitCreatePage } from './pages/visits/VisitCreatePage'
import { VisitEditPage } from './pages/visits/VisitEditPage'

/** 06 화면 명세 0.1절 라우트. ToastProvider는 페이지 이동 후에도 토스트가 보이도록 레이아웃 최상단에 둔다. */
export const router = createBrowserRouter([
  {
    element: (
      <ToastProvider>
        <Layout />
      </ToastProvider>
    ),
    children: [
      { path: '/', element: <HomePage /> },
      { path: '/doctors', element: <DoctorListPage /> },
      { path: '/doctors/new', element: <DoctorFormPage key="create" mode="create" /> },
      { path: '/doctors/:id', element: <DoctorDetailPage /> },
      { path: '/doctors/:id/edit', element: <DoctorFormPage key="edit" mode="edit" /> },
      { path: '/doctors/:id/visits/new', element: <VisitCreatePage /> },
      { path: '/visits/:id/edit', element: <VisitEditPage /> },
      { path: '/questions', element: <QuestionListPage /> },
      { path: '/questions/:id', element: <QuestionDetailPage /> },
      { path: '/commitments/:id/edit', element: <CommitmentEditPage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
