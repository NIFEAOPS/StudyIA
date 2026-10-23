import { ArrowLeft } from 'lucide-react'
import { Link, Outlet, useLocation } from 'react-router-dom'
import { AuthLinks } from './AuthLinks'

export function AuthPage() {
  const pathname = useLocation().pathname
  const isRegister = pathname === '/register'
  const isVerification = pathname === '/verify-email'
  const isRecovery = pathname === '/forgot-password' || pathname === '/reset-password'
  return <div className="auth-shell">
    <div className="auth-decoration"><div className="auth-orbit orbit-one" /><div className="auth-orbit orbit-two" /><span className="auth-decoration-mark">study<span>ai</span></span><p>Tu siguiente nivel<br /><em>empieza con dirección.</em></p></div>
    <main className="auth-main"><Link className="auth-back" to="/"><ArrowLeft size={16} /> Volver al inicio</Link><div className="auth-card"><Link className="auth-brand" to="/"><img className="brand-logo" src="/logostudyia.png" alt="Study AI" /></Link><div className="auth-card-heading"><span className="eyebrow">{isVerification ? 'Confirmar correo' : isRecovery ? 'Recuperar acceso' : isRegister ? 'Crear una cuenta' : 'Bienvenido de nuevo'}</span><h1>{isVerification ? 'Una última señal.' : isRecovery ? 'Recupera tu cuenta.' : isRegister ? 'Empieza tu ruta.' : 'Continúa tu camino.'}</h1><p>{isVerification ? 'Confirma tu correo para activar tu espacio de aprendizaje.' : isRecovery ? 'Te enviaremos un enlace seguro para crear una nueva contraseña.' : isRegister ? 'Tu perfil será el primer paso hacia un aprendizaje con intención.' : 'Retoma el plan que estás construyendo para tu futuro.'}</p></div><Outlet />{!isVerification && !isRecovery && <AuthLinks />}</div></main>
  </div>
}
