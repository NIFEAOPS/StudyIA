import { useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { Eye, EyeOff, LoaderCircle, LockKeyhole, Mail } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { getDashboard } from '../api'
import { apiClient } from '../apiClient'
import { useAuthStore } from './authStore'
import { AuthFieldError } from './AuthFieldError'
import { loginSchema, type LoginFormValues } from './schemas'
import type { AuthResponse } from './types'

export function LoginForm() {
  const navigate = useNavigate()
  const location = useLocation()
  const setSession = useAuthStore((state) => state.setSession)
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const { register, handleSubmit, formState: { errors } } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    mode: 'onTouched',
  })

  async function onSubmit(values: LoginFormValues) {
    setIsLoading(true)
    try {
      const { data } = await apiClient.post<AuthResponse>('/auth/login', values)
      setSession(data)
      toast.success(`Qué bueno verte, ${data.name.split(' ')[0]}.`)
      const requestedDestination = (location.state as { from?: string } | null)?.from
      let destination = requestedDestination ?? '/app'
      if (!requestedDestination) {
        try {
          await getDashboard()
          destination = '/progress'
        } catch {
          destination = '/app'
        }
      }
      navigate(destination, { replace: true })
    } catch {
      toast.error('Credenciales inválidas', { description: 'Revisa tu correo y contraseña.' })
    } finally {
      setIsLoading(false)
    }
  }

  return <form className="auth-form" onSubmit={handleSubmit(onSubmit)} noValidate>
    <label className="auth-input-field"><span>Correo</span><div className="auth-input-wrap"><Mail size={17} /><input type="email" autoComplete="email" placeholder="tu@universidad.edu" {...register('email')} /></div><AuthFieldError message={errors.email?.message} /></label>
    <label className="auth-input-field"><span>Contraseña</span><div className="auth-input-wrap"><LockKeyhole size={17} /><input type={showPassword ? 'text' : 'password'} autoComplete="current-password" placeholder="Tu contraseña" {...register('password')} /><button type="button" className="password-toggle" onClick={() => setShowPassword((current) => !current)} aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}>{showPassword ? <EyeOff size={17} /> : <Eye size={17} />}</button></div><AuthFieldError message={errors.password?.message} /></label>
    <button className="auth-submit" type="submit" disabled={isLoading}>{isLoading ? <><LoaderCircle className="spin" size={17} /> Entrando...</> : 'Entrar a Study AI'}</button>
    <Link className="forgot-password-link" to="/forgot-password">¿Olvidaste tu contraseña?</Link>
  </form>
}
