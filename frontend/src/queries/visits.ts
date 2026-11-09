import { useMutation, useQuery, useQueryClient, type QueryClient } from '@tanstack/react-query'
import { createVisit, deleteVisit, getVisit, updateVisit } from '../api/visits'
import type { VisitCreateRequest, VisitUpdateRequest } from '../api/types'
import { keys } from './keys'

export function useVisitQuery(id: number) {
  return useQuery({ queryKey: keys.visit(id), queryFn: () => getVisit(id) })
}

/** 방문 작성·수정·삭제: ['visit', id], ['doctor', doctorId], ['home'], ['questions'], ['doctors'] 무효화. */
function invalidateVisit(queryClient: QueryClient, visitId: number, doctorId: number) {
  return Promise.all([
    queryClient.invalidateQueries({ queryKey: keys.visit(visitId) }),
    queryClient.invalidateQueries({ queryKey: keys.doctor(doctorId) }),
    queryClient.invalidateQueries({ queryKey: keys.home }),
    queryClient.invalidateQueries({ queryKey: ['questions'] }),
    queryClient.invalidateQueries({ queryKey: ['doctors'] }),
  ])
}

export function useCreateVisitMutation(doctorId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: VisitCreateRequest) => createVisit(doctorId, body),
    onSuccess: (visit) => invalidateVisit(queryClient, visit.id, doctorId),
  })
}

export function useUpdateVisitMutation(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: VisitUpdateRequest) => updateVisit(id, body),
    onSuccess: (visit) => invalidateVisit(queryClient, id, visit.doctorId),
  })
}

export function useDeleteVisitMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id }: { id: number; doctorId: number }) => deleteVisit(id),
    onSuccess: (_data, { id, doctorId }) => invalidateVisit(queryClient, id, doctorId),
  })
}
