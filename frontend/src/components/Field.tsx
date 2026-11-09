import type { ReactNode } from 'react'

type FieldProps = {
  label: string
  htmlFor: string
  required?: boolean
  error?: string
  hint?: ReactNode
  children: ReactNode
}

/**
 * 라벨 + 입력 + 안내 + 에러 묶음 (07 문서 4.4절).
 * 입력에는 id={htmlFor}, aria-invalid, aria-describedby={fieldDescribedBy(...)} (lib/fieldErrors.ts)를 직접 붙인다.
 */
export function Field({ label, htmlFor, required, error, hint, children }: FieldProps) {
  return (
    <div className="field">
      <label className="field__label" htmlFor={htmlFor}>
        {label}
        {required && (
          <span className="field__req" aria-hidden="true">
            *
          </span>
        )}
      </label>
      {children}
      {hint && (
        <p className="field__hint" id={`${htmlFor}-hint`}>
          {hint}
        </p>
      )}
      {error && (
        <p className="field__error" id={`${htmlFor}-error`}>
          {error}
        </p>
      )}
    </div>
  )
}
