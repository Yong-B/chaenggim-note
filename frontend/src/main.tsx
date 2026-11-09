import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { ApiError } from './api/client'
import { router } from './router'
import './styles/app.css'

/** staleTime 0, 404면 재시도 안 함·그 외 1회, 창 포커스 시 다시 가져오기 (07 문서 3.3절). */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 0,
      retry: (failureCount, error) => !(error instanceof ApiError && error.status === 404) && failureCount < 1,
      refetchOnWindowFocus: true,
    },
  },
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  </StrictMode>,
)
