import { jsonBody, request } from './client'
import type { DoctorDetailResponse, DoctorRequest, DoctorResponse, DoctorSummary } from './types'

export const searchDoctors = (q: string) =>
  request<DoctorSummary[]>(q ? `/doctors?q=${encodeURIComponent(q)}` : '/doctors')

export const createDoctor = (body: DoctorRequest) =>
  request<DoctorResponse>('/doctors', { method: 'POST', body: jsonBody(body) })

export const getDoctor = (id: number) => request<DoctorDetailResponse>(`/doctors/${id}`)

export const updateDoctor = (id: number, body: DoctorRequest) =>
  request<DoctorResponse>(`/doctors/${id}`, { method: 'PUT', body: jsonBody(body) })

export const deleteDoctor = (id: number) => request<null>(`/doctors/${id}`, { method: 'DELETE' })
