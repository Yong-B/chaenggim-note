export type FieldError = { field: string; message: string }

type ApiResponse<T> = {
  success: boolean
  data: T
  error: { code: string; message: string; fieldErrors: FieldError[] } | null
}

export class ApiError extends Error {
  constructor(
    public status: number,
    public code: string,
    message: string,
    public fieldErrors: FieldError[],
  ) {
    super(message)
  }
}

export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`/api${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...init,
  })
  // 백엔드는 모든 응답을 { success, data, error }로 감싼다 (05 API 명세 1절)
  const body = (await res.json().catch(() => null)) as ApiResponse<T> | null
  if (!res.ok || !body?.success) {
    throw new ApiError(
      res.status,
      body?.error?.code ?? 'UNKNOWN',
      body?.error?.message ?? '일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.',
      body?.error?.fieldErrors ?? [],
    )
  }
  return body.data
}

export function jsonBody(value: unknown): RequestInit['body'] {
  return JSON.stringify(value)
}
