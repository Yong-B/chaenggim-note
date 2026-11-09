import { useMutation, useQuery, useQueryClient, type QueryClient } from '@tanstack/react-query'
import {
  answerQuestion,
  deleteQuestion,
  deliverQuestion,
  getQuestion,
  listQuestions,
  startChecking,
  updateQuestion,
} from '../api/questions'
import type { AnswerRequest, ProductTag, QuestionResponse, QuestionStatus } from '../api/types'
import { keys } from './keys'

export function useQuestionsQuery(status: QuestionStatus, product: ProductTag | null) {
  return useQuery({ queryKey: keys.questions(status, product), queryFn: () => listQuestions(status, product) })
}

export function useQuestionQuery(id: number) {
  return useQuery({ queryKey: keys.question(id), queryFn: () => getQuestion(id) })
}

/** 질문 수정·상태 변경·삭제: ['question', id], ['doctor', doctorId], ['home'], ['questions'] 무효화. */
function invalidateQuestion(queryClient: QueryClient, id: number, doctorId: number) {
  return Promise.all([
    queryClient.invalidateQueries({ queryKey: keys.question(id) }),
    queryClient.invalidateQueries({ queryKey: keys.doctor(doctorId) }),
    queryClient.invalidateQueries({ queryKey: keys.home }),
    queryClient.invalidateQueries({ queryKey: ['questions'] }),
  ])
}

function useQuestionChange<V>(id: number, fn: (variables: V) => Promise<QuestionResponse>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: fn,
    onSuccess: (question) => invalidateQuestion(queryClient, id, question.doctor.id),
  })
}

export const useUpdateQuestionMutation = (id: number) =>
  useQuestionChange(id, (content: string) => updateQuestion(id, { content }))

export const useStartCheckingMutation = (id: number) => useQuestionChange(id, () => startChecking(id))

export const useAnswerMutation = (id: number) =>
  useQuestionChange(id, (body: AnswerRequest) => answerQuestion(id, body))

export const useDeliverMutation = (id: number) => useQuestionChange(id, () => deliverQuestion(id))

/**
 * 삭제: ['question', id]는 무효화 대신 제거해야 하지만, 상세 화면이 떠 있는 동안 제거하면 다시 요청해 404가 잠깐 보이므로
 * 제거는 화면 이동 뒤에 removeQuestionQuery로 한다.
 */
export function useDeleteQuestionMutation(id: number, doctorId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => deleteQuestion(id),
    onSuccess: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: keys.doctor(doctorId) }),
        queryClient.invalidateQueries({ queryKey: keys.home }),
        queryClient.invalidateQueries({ queryKey: ['questions'] }),
      ]),
  })
}

export function removeQuestionQuery(queryClient: QueryClient, id: number) {
  queryClient.removeQueries({ queryKey: keys.question(id) })
}
