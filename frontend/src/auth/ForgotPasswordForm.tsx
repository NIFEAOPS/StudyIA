import { useState } from 'react'
import { LoaderCircle, Mail } from 'lucide-react'
import { Link } from 'react-router-dom'
import { apiClient, apiErrorMessage } from '../apiClient'

export function ForgotPasswordForm() {
  const [email, setEmail] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [submitted, setSubmitted] = useState(false)
  const [error, setError] = useState('')

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsLoading(true)
    setError('')
    try {
      await apiClient.post('/auth/forgot-password', { email })
      setSubmitted(true)
    } catch (requestError) {
      setError(apiErrorMessage(requestError, 'No se pudo procesar la solicitud.'))
    } finally {
      setIsLoading(false)
    }
  }

  if (submitted) {
    return <div className="auth-confirmation">
      <span className="verification-icon"><Mail size={23} /></span>
      <h2>Revisa tu correo</h2>
      <p>Si existe una cuenta asociada a ese correo, recibirás un enlace para restablecer tu contraseña. El enlace será válido durante 10 minutos.</p>
      <Link className="auth-submit auth-submit-link" to="/login">Volver al inicio de sesión</Link>
    </div>
  }

  return <form className="auth-form" onSubmit={(event) => void submit(event)} noValidate>
    <label className="auth-input-field"><span>Correo de tu cuenta</span><div className="auth-input-wrap"><Mail size={17} /><input type="email" required autoComplete="email" placeholder="tu@universidad.edu" value={email} onChange={(event) => setEmail(event.target.value)} /></div></label>
    {error && <small className="auth-field-error">{error}</small>}
    <button className="auth-submit" type="submit" disabled={isLoading}>{isLoading ? <><LoaderCircle className="spin" size={17} /> Enviando enlace...</> : 'Enviar enlace de recuperación'}</button>
    <Link className="verification-back" to="/login">← Volver al inicio de sesión</Link>
  </form>
}
