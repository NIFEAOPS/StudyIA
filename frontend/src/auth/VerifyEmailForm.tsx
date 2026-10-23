import { useState } from 'react'
import { ArrowLeft, LoaderCircle, MailCheck } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { apiClient } from '../apiClient'
import { useAuthStore } from './authStore'
import type { AuthResponse } from './types'

type VerificationValues = { code: string }

export function VerifyEmailForm() {
  const location = useLocation()
  const navigate = useNavigate()
  const setSession = useAuthStore((state) => state.setSession)
  const email = (location.state as { email?: string } | null)?.email ?? ''
  const [isLoading, setIsLoading] = useState(false)
  const [isResending, setIsResending] = useState(false)
  const { register, handleSubmit, formState: { errors } } = useForm<VerificationValues>()

  async function onSubmit(values: VerificationValues) {
    if (!email) {
      toast.error('Falta el correo', { description: 'Vuelve al registro para solicitar un código.' })
      return
    }
    setIsLoading(true)
    try {
      const { data } = await apiClient.post<AuthResponse>('/auth/verify-email', { email, code: values.code })
      setSession(data)
      toast.success('Correo confirmado.')
      navigate('/app', { replace: true })
    } catch {
      toast.error('Código inválido', { description: 'Comprueba el código o solicita uno nuevo.' })
    } finally {
      setIsLoading(false)
    }
  }

  async function resendCode() {
    if (!email) return
    setIsResending(true)
    try {
      await apiClient.post('/auth/resend-verification', { email })
      toast.success('Código reenviado.')
    } catch {
      toast.error('No pudimos reenviar el código.')
    } finally {
      setIsResending(false)
    }
  }

  return <form className="auth-form" onSubmit={handleSubmit(onSubmit)} noValidate>
    <div className="verification-icon"><MailCheck size={23} /></div>
    <p className="verification-copy">Enviamos un código de 6 dígitos a <strong>{email || 'tu correo'}</strong>.</p>
    <label className="auth-input-field"><span>Código de verificación</span><input className="verification-code-input" inputMode="numeric" autoComplete="one-time-code" maxLength={6} placeholder="000000" {...register('code', { required: 'Escribe el código', pattern: { value: /^\d{6}$/, message: 'Usa 6 dígitos' } })} />{errors.code && <small className="auth-field-error">{errors.code.message}</small>}</label>
    <button className="auth-submit" type="submit" disabled={isLoading}>{isLoading ? <><LoaderCircle className="spin" size={17} /> Confirmando...</> : 'Confirmar correo'}</button>
    <button className="verification-resend" type="button" onClick={() => void resendCode()} disabled={isResending}>{isResending ? 'Enviando...' : 'Reenviar código'}</button>
    <Link className="verification-back" to="/register"><ArrowLeft size={14} /> Cambiar correo</Link>
  </form>
}