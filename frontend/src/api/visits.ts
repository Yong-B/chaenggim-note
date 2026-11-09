import { jsonBody, request } from './client'
import type { VisitCreateRequest, VisitDetailResponse, VisitResponse, VisitUpdateRequest } from './types'

export const createVisit = (doctorId: number, body: VisitCreateRequest) =>
  request<VisitResponse>(`/doctors/${doctorId}/visits`, { method: 'POST', body: jsonBody(body) })

export const getVisit = (id: number) => request<VisitDetailResponse>(`/visits/${id}`)

export const updateVisit = (id: number, body: VisitUpdateRequest) =>
  request<VisitResponse>(`/visits/${id}`, { method: 'PUT', body: jsonBody(body) })

export const deleteVisit = (id: number) => request<null>(`/visits/${id}`, { method: 'DELETE' })
