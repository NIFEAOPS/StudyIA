import { Compass, LayoutDashboard, LogOut, UserRound } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'

type WorkspaceHeaderProps = {
  userName?: string
  onLogout: () => void | Promise<void>
}

export function WorkspaceHeader({ userName, onLogout }: WorkspaceHeaderProps) {
  const location = useLocation()
  return <header className="progress-header">
    <Link className="brand" to="/roadmap"><img className="brand-logo" src="/logostudyia.png" alt="Study AI" /></Link>
    <div className="progress-header-label">STUDYIA / TU ESPACIO</div>
    <nav>
      <Link to="/roadmap"><Compass size={15} /> Roadmap</Link>
      <Link className={location.pathname === '/progress' ? 'progress-nav-active' : undefined} to="/progress"><LayoutDashboard size={15} /> Mi progreso</Link>
      <Link to="/app">Nueva ruta</Link>
      <Link className={location.pathname === '/profile' ? 'progress-nav-active' : undefined} to="/profile"><UserRound size={15} /> Perfil</Link>
      <span className="progress-user">{userName?.split(' ')[0]}</span>
      <button className="logout-button" onClick={() => void onLogout()}><LogOut size={14} /> Salir</button>
    </nav>
  </header>
}
