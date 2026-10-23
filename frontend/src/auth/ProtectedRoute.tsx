import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { LoaderCircle } from 'lucide-react'
import { useAuthStore } from './authStore'

export function ProtectedRoute() {
  const { isAuthenticated, isHydrating } = useAuthStore()
  const location = useLocation()

  if (isHydrating) {
    return <div className="auth-loading" role="status"><LoaderCircle className="spin" size={22} /> Verificando tu sesión...</div>
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return <Outlet />
}
