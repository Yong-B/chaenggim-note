import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { createDoctor, deleteDoctor, getDoctor, searchDoctors, updateDoctor } from '../api/doctors'
import type { DoctorRequest } from '../api/types'
import { keys } from './keys'

export function useDoctorsQuery(q: string) {
  return useQuery({ queryKey: keys.doctors(q), queryFn: () => searchDoctors(q) })
}

export function useDoctorQuery(id: number) {
  return useQuery({ queryKey: keys.doctor(id), queryFn: () => getDoctor(id) })
}

/** 등록·수정: ['doctors'], ['doctor', id], ['home'] 무효화. */
export function useCreateDoctorMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: DoctorRequest) => createDoctor(body),
    onSuccess: (doctor) =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: ['doctors'] }),
        queryClient.invalidateQueries({ queryKey: keys.doctor(doctor.id) }),
        queryClient.invalidateQueries({ queryKey: keys.home }),
      ]),
  })
}

export function useUpdateDoctorMutation(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: DoctorRequest) => updateDoctor(id, body),
    onSuccess: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: ['doctors'] }),
        queryClient.invalidateQueries({ queryKey: keys.doctor(id) }),
        queryClient.invalidateQueries({ queryKey: keys.home }),
      ]),
  })
}

/** 삭제: ['doctors'], ['home'], ['questions'] 무효화, ['doctor', id]는 제거. */
export function useDeleteDoctorMutation(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => deleteDoctor(id),
    onSuccess: () => {
      queryClient.removeQueries({ queryKey: keys.doctor(id) })
      return Promise.all([
        queryClient.invalidateQueries({ queryKey: ['doctors'] }),
        queryClient.invalidateQueries({ queryKey: keys.home }),
        queryClient.invalidateQueries({ queryKey: ['questions'] }),
      ])
    },
  })
}
