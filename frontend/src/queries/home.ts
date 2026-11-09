import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { resetData } from '../api/admin'
import { getHome } from '../api/home'
import { keys } from './keys'

export function useHomeQuery() {
  return useQuery({ queryKey: keys.home, queryFn: getHome })
}

/** 초기화: 전체 무효화 (05 API 명세 9절). */
export function useResetMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: resetData,
    onSuccess: () => queryClient.invalidateQueries(),
  })
}
