import { ApiError } from '../api/client'

const FALLBACK = '일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.'

/** 변경 요청 실패를 토스트로 보여 줄 문구. 서버가 보낸 메시지(409 등)를 그대로 쓴다. */
export function errorMessage(error: unknown): string {
  if (error instanceof ApiError) return error.message
  return FALLBACK
}
