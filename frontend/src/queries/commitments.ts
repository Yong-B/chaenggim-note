import { useMutation, useQuery, useQueryClient, type QueryClient } from '@tanstack/react-query'
import {
  completeCommitment,
  deleteCommitment,
  getCommitment,
  reopenCommitment,
  updateCommitment,
} from '../api/commitments'
import type { CommitmentUpdateRequest } from '../api/types'
import { keys } from './keys'

export function useCommitmentQuery(id: number) {
  return useQuery({ queryKey: keys.commitment(id), queryFn: () => getCommitment(id) })
}

/** 약속 수정·완료·취소·삭제: ['commitment', id], ['doctor', doctorId], ['home'] 무효화. */
function invalidateCommitment(queryClient: QueryClient, id: number, doctorId: number) {
  return Promise.all([
    queryClient.invalidateQueries({ queryKey: keys.commitment(id) }),
    queryClient.invalidateQueries({ queryKey: keys.doctor(doctorId) }),
    queryClient.invalidateQueries({ queryKey: keys.home }),
  ])
}

export function useUpdateCommitmentMutation(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: CommitmentUpdateRequest) => updateCommitment(id, body),
    onSuccess: (c) => invalidateCommitment(queryClient, id, c.doctor.id),
  })
}

/** 홈·상세 목록에서 여러 약속에 쓰므로 id를 변수로 받는다. */
export function useCompleteCommitmentMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => completeCommitment(id),
    onSuccess: (c) => invalidateCommitment(queryClient, c.id, c.doctor.id),
  })
}

export function useReopenCommitmentMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => reopenCommitment(id),
    onSuccess: (c) => invalidateCommitment(queryClient, c.id, c.doctor.id),
  })
}

/**
 * 삭제: ['commitment', id]는 무효화 대신 제거해야 하지만, 수정 화면이 떠 있는 동안 제거하면 다시 요청해 404가 잠깐 보이므로
 * 수정 화면에서는 화면 이동 뒤에 removeCommitmentQuery로 제거한다(목록에서 삭제할 때는 그 쿼리를 쓰는 화면이 없다).
 */
export function useDeleteCommitmentMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id }: { id: number; doctorId: number }) => deleteCommitment(id),
    onSuccess: (_data, { doctorId }) =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: keys.doctor(doctorId) }),
        queryClient.invalidateQueries({ queryKey: keys.home }),
      ]),
  })
}

export function removeCommitmentQuery(queryClient: QueryClient, id: number) {
  queryClient.removeQueries({ queryKey: keys.commitment(id) })
}
