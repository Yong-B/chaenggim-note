import { jsonBody, request } from './client'
import type { CommitmentResponse, CommitmentUpdateRequest } from './types'

export const getCommitment = (id: number) => request<CommitmentResponse>(`/commitments/${id}`)

export const updateCommitment = (id: number, body: CommitmentUpdateRequest) =>
  request<CommitmentResponse>(`/commitments/${id}`, { method: 'PUT', body: jsonBody(body) })

export const completeCommitment = (id: number) =>
  request<CommitmentResponse>(`/commitments/${id}/complete`, { method: 'POST' })

export const reopenCommitment = (id: number) =>
  request<CommitmentResponse>(`/commitments/${id}/reopen`, { method: 'POST' })

export const deleteCommitment = (id: number) => request<null>(`/commitments/${id}`, { method: 'DELETE' })
