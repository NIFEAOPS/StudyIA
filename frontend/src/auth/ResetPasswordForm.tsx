import { useState } from 'react'
import { Eye, EyeOff, KeyRound, LoaderCircle } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'
import { apiClient, apiErrorMessage } from '../apiClient'

const requirements = [
  { label: 'Al menos 8 caracteres', test: (value: string) => value.length >= 8 },
  { label: 'Una mayúscula y una minúscula', test: (value: string) => /[A-Z]/.test(value) && /[a-z]/.test(value) },
  { label: 'Un número y un símbolo', test: (value: string) => /[0-9]/.test(value) && /[^A-Za-z0-9]/.test(value) },
]

export function ResetPasswordForm() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''
  const [password, setPassword] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [visible, setVisible] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [completed, setCompleted] = useState(false)
  const [error, setError] = useState('')

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!token) { setError('El enlace de recuperación no es válido.'); return }
    if (password !== confirmation) { setError('Las contraseñas no coinciden.'); return }
    if (requirements.some((requirement) => !requirement.test(password))) {
      setError('La contraseña no cumple todos los requisitos de seguridad.')
      return
    }
    setIsLoading(true); setError('')
    try {
      await apiClient.post('/auth/reset-password', { token, newPassword: password })
      setCompleted(true)
    } catch (requestError) {
      setError(apiErrorMessage(requestError, 'El enlace no es válido o ya expiró.'))
    } finally { setIsLoading(false) }
  }

  if (completed) {
    return <div className="auth-confirmation"><span className="verification-icon"><KeyRound size={23} /></span><h2>Contraseña actualizada</h2><p>Ya puedes iniciar sesión con tu nueva contraseña.</p><Link className="auth-submit auth-submit-link" to="/login">Ir al inicio de sesión</Link></div>
  }

  return <form className="auth-form" onSubmit={(event) => void submit(event)} noValidate>
    <label className="auth-input-field"><span>Nueva contraseña</span><div className="auth-input-wrap"><KeyRound size={17} /><input type={visible ? 'text' : 'password'} required autoComplete="new-password" placeholder="Crea una contraseña segura" value={password} onChange={(event) => setPassword(event.target.value)} /><button type="button" className="password-toggle" onClick={() => setVisible((current) => !current)} aria-label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}>{visible ? <EyeOff size={17} /> : <Eye size={17} />}</button></div></label>
    <div className="password-requirements">{requirements.map((requirement) => <span className={requirement.test(password) ? 'valid' : ''} key={requirement.label}>✓ {requirement.label}</span>)}</div>
    <label className="auth-input-field"><span>Confirmar contraseña</span><div className="auth-input-wrap"><KeyRound size={17} /><input type={visible ? 'text' : 'password'} required autoComplete="new-password" placeholder="Repite tu contraseña" value={confirmation} onChange={(event) => setConfirmation(event.target.value)} /></div></label>
    {error && <small className="auth-field-error">{error}</small>}
    <button className="auth-submit" type="submit" disabled={isLoading}>{isLoading ? <><LoaderCircle className="spin" size={17} /> Actualizando...</> : 'Actualizar contraseña'}</button>
  </form>
}
