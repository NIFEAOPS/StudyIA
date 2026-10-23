import { Link, useLocation } from 'react-router-dom'

export function AuthLinks() {
  const isRegister = useLocation().pathname === '/register'
  return <p className="auth-switch">{isRegister ? '¿Ya tienes cuenta?' : '¿Primera vez en Study AI?'} <Link to={isRegister ? '/login' : '/register'}>{isRegister ? 'Inicia sesión' : 'Crea tu cuenta'}</Link></p>
}
