import { useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { Eye, EyeOff, LoaderCircle, LockKeyhole, Mail, UserRound } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { apiClient } from '../apiClient'
import { AuthFieldError } from './AuthFieldError'
import { registerSchema, type RegisterFormValues } from './schemas'

export function RegisterForm() {
  const navigate = useNavigate()
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const { register, handleSubmit, formState: { errors } } = useForm<RegisterFormValues>({ resolver: zodResolver(registerSchema), mode: 'onTouched' })

  async function onSubmit(values: RegisterFormValues) {
    setIsLoading(true)
    try {
      const { confirmPassword: _, ...payload } = values
      const { data } = await apiClient.post<{ email: string }>('/auth/register', payload)
      toast.success('Revisa tu correo para confirmar la cuenta.')
      navigate('/verify-email', { replace: true, state: { email: data.email } })
    } catch {
      toast.error('No pudimos crear tu cuenta', { description: 'Verifica tus datos e inténtalo de nuevo.' })
    } finally {
      setIsLoading(false)
    }
  }

  return <form className="auth-form" onSubmit={handleSubmit(onSubmit)} noValidate>
    <label className="auth-input-field"><span>Nombre</span><div className="auth-input-wrap"><UserRound size={17} /><input autoComplete="name" placeholder="Tu nombre" {...register('name')} /></div><AuthFieldError message={errors.name?.message} /></label>
    <label className="auth-input-field"><span>Correo universitario</span><div className="auth-input-wrap"><Mail size={17} /><input type="email" autoComplete="email" placeholder="tu@universidad.edu" {...register('email')} /></div><AuthFieldError message={errors.email?.message} /></label>
    <label className="auth-input-field"><span>Contraseña</span><div className="auth-input-wrap"><LockKeyhole size={17} /><input type={showPassword ? 'text' : 'password'} autoComplete="new-password" placeholder="Mínimo 10 caracteres" {...register('password')} /><button type="button" className="password-toggle" onClick={() => setShowPassword((current) => !current)} aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}>{showPassword ? <EyeOff size={17} /> : <Eye size={17} />}</button></div><AuthFieldError message={errors.password?.message} /></label>
    <label className="auth-input-field"><span>Confirmar contraseña</span><div className="auth-input-wrap"><LockKeyhole size={17} /><input type={showPassword ? 'text' : 'password'} autoComplete="new-password" placeholder="Repite tu contraseña" {...register('confirmPassword')} /></div><AuthFieldError message={errors.confirmPassword?.message} /></label>
    <button className="auth-submit" type="submit" disabled={isLoading}>{isLoading ? <><LoaderCircle className="spin" size={17} /> Creando cuenta...</> : 'Crear mi cuenta'}</button>
  </form>
}
