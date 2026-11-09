import { jsonBody, request } from './client'
import type {
  AnswerRequest,
  QuestionListResponse,
  QuestionResponse,
  QuestionStatus,
  QuestionUpdateRequest,
} from './types'

export const listQuestions = (status: QuestionStatus) =>
  request<QuestionListResponse>(`/questions?status=${status}`)

export const getQuestion = (id: number) => request<QuestionResponse>(`/questions/${id}`)

export const updateQuestion = (id: number, body: QuestionUpdateRequest) =>
  request<QuestionResponse>(`/questions/${id}`, { method: 'PATCH', body: jsonBody(body) })

export const startChecking = (id: number) =>
  request<QuestionResponse>(`/questions/${id}/start-checking`, { method: 'POST' })

export const answerQuestion = (id: number, body: AnswerRequest) =>
  request<QuestionResponse>(`/questions/${id}/answer`, { method: 'POST', body: jsonBody(body) })

export const deliverQuestion = (id: number) =>
  request<QuestionResponse>(`/questions/${id}/deliver`, { method: 'POST' })

export const deleteQuestion = (id: number) => request<null>(`/questions/${id}`, { method: 'DELETE' })
