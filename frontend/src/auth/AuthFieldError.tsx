import { CircleAlert } from 'lucide-react'

type AuthFieldErrorProps = { message?: string }

export function AuthFieldError({ message }: AuthFieldErrorProps) {
  if (!message) return null
  return <span className="auth-field-error" role="alert"><CircleAlert size={13} /> {message}</span>
}
