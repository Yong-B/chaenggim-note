import type { FieldError } from '../api/client'

/** 서버 fieldErrors 배열 → { field: message }. 같은 필드가 여럿이면 첫 번째만. */
export function toFieldErrorMap(fieldErrors: FieldError[]): Record<string, string> {
  const map: Record<string, string> = {}
  for (const { field, message } of fieldErrors) {
    if (!(field in map)) map[field] = message
  }
  return map
}

/**
 * 폼의 입력칸과 매칭되는 에러는 errors로, 나머지는 formErrors로 나눈다 (07 문서 4.3절).
 * knownFields: 이 폼이 표시할 수 있는 필드 이름 (예: 'name', 'questions[0].content').
 */
export function splitFieldErrors(
  fieldErrors: FieldError[],
  knownFields: (field: string) => boolean,
): { errors: Record<string, string>; formErrors: string[] } {
  const errors: Record<string, string> = {}
  const formErrors: string[] = []
  for (const [field, message] of Object.entries(toFieldErrorMap(fieldErrors))) {
    if (knownFields(field)) errors[field] = message
    else formErrors.push(message)
  }
  return { errors, formErrors }
}

/** 입력의 aria-describedby 값: 에러가 있으면 "{id}-error", 안내가 있으면 "{id}-hint". */
export function fieldDescribedBy(id: string, error?: string, hasHint?: boolean): string | undefined {
  const ids = [error ? `${id}-error` : null, hasHint ? `${id}-hint` : null].filter(Boolean)
  return ids.length ? ids.join(' ') : undefined
}

/** 입력값이 바뀐 필드의 에러만 지운다 (07 문서 4.3절). */
export function clearError(errors: Record<string, string>, field: string): Record<string, string> {
  if (!(field in errors)) return errors
  const next = { ...errors }
  delete next[field]
  return next
}
