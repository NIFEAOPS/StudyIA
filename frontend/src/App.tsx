import { useEffect, useState } from 'react'
import { ArrowRight, ArrowUpRight, CalendarDays, CheckCircle2, Code2, Compass, Flame, Layers3, LogOut, Menu, ShieldCheck, Sparkles } from 'lucide-react'
import { Link, Navigate, Route, Routes, useNavigate } from 'react-router-dom'
import { completeActivity, evaluateAssessment, generateAssessment, getDashboard } from './api'
import { AuthPage } from './auth/AuthPage'
import { LoginForm } from './auth/LoginForm'
import { RegisterForm } from './auth/RegisterForm'
import { VerifyEmailForm } from './auth/VerifyEmailForm'
import { ForgotPasswordForm } from './auth/ForgotPasswordForm'
import { ResetPasswordForm } from './auth/ResetPasswordForm'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { useAuthStore } from './auth/authStore'
import { apiErrorMessage } from './apiClient'
import { OnboardingFlow } from './components/OnboardingFlow'
import { RoadmapTimeline } from './components/RoadmapTimeline'
import { ProfilePage } from './ProfilePage'
import { WorkspaceHeader } from './WorkspaceHeader'
import type { AssessmentAnswer, AssessmentQuestion, Dashboard, StudentProfile } from './types'
import './styles.css'

function PublicHome() {
  const { isAuthenticated, isHydrating } = useAuthStore()
  const [destination, setDestination] = useState<string | null>(null)

  useEffect(() => {
    if (!isAuthenticated || isHydrating) {
      setDestination(null)
      return
    }

    let active = true
    void getDashboard()
      .then(() => {
        if (active) setDestination('/progress')
      })
      .catch(() => {
        if (active) setDestination('/app')
      })

    return () => {
      active = false
    }
  }, [isAuthenticated, isHydrating])

  if (isHydrating) {
    return <div className="progress-loading" role="status">Verificando tu sesión...</div>
  }

  if (isAuthenticated) {
    return destination
      ? <Navigate to={destination} replace />
      : <div className="progress-loading" role="status">Cargando tu espacio...</div>
  }

  return (
    <div className="app-shell landing-shell">
      <header className="topbar landing-topbar">
        <Link className="brand" to="/">
          <img className="brand-logo" src="/logostudyia.png" alt="Study AI" />
        </Link>
        <div className="landing-nav-label">PERSONAL LEARNING OS <span>●</span></div>
        <nav>
          <a href="#como-funciona">Cómo funciona</a>
          <Link to="/login">Entrar</Link>
          <Link className="nav-cta" to="/register">Empezar gratis <ArrowUpRight size={14} /></Link>
        </nav>
      </header>

      <main>
        <section className="landing-hero">
          <div className="landing-hero-copy">
            <div className="status-pill"><span /> Beta privada · aprende con intención</div>
            <h1>Tu futuro no necesita más <em>ruido.</em><br />Necesita una ruta.</h1>
            <p className="hero-lede">StudyIA convierte tus metas, tu nivel actual y el tiempo que tienes en un plan claro para aprender habilidades que sí te acercan a donde quieres llegar.</p>
            <div className="landing-actions">
              <Link className="primary-button public-cta" to="/register">Construir mi ruta <ArrowUpRight size={17} /></Link>
              <a className="landing-text-link" href="#como-funciona">Descubre cómo funciona <ArrowUpRight size={15} /></a>
            </div>
            <div className="landing-proof"><div className="avatar-stack"><span>✦</span><span>◎</span><span>＋</span></div><p><strong>Aprende a tu manera.</strong><br />Una ruta que empieza exactamente donde estás.</p></div>
          </div>

          <div className="landing-product-shot" aria-label="Vista previa de una ruta de aprendizaje personalizada">
            <div className="shot-glow" />
            <div className="shot-window">
              <div className="shot-topbar"><span className="shot-dots"><i /><i /><i /></span><span>studyai / tu ruta</span><span className="shot-live">● EN VIVO</span></div>
              <div className="shot-content">
                <div className="shot-welcome"><span className="eyebrow"><Sparkles size={13} /> RUTA PERSONALIZADA</span><h2>De curiosidad a<br /><strong>habilidad real.</strong></h2><p>Tu siguiente paso está listo.</p></div>
                <div className="shot-progress"><div className="shot-progress-head"><span>Desarrollo frontend</span><strong>24%</strong></div><div className="shot-progress-bar"><i /></div><small>Semana 2 de 8 · ritmo recomendado</small></div>
                <div className="shot-next"><div className="next-icon"><Code2 size={16} /></div><div><small>SIGUIENTE PASO</small><strong>Construye tu primera interfaz</strong><span>HTML + CSS · 45 min</span></div><ArrowUpRight size={16} /></div>
                <div className="shot-mini-grid"><span><strong>03</strong><small>objetivos</small></span><span><strong>08</strong><small>semanas</small></span><span><strong>∞</strong><small>sin ruido</small></span></div>
              </div>
            </div>
          </div>
        </section>

        <section className="landing-marquee"><span>UNA MEJOR FORMA DE APRENDER</span><i /> <span>MENOS CONTENIDO PERDIDO</span><i /> <span>MÁS PROGRESO REAL</span><i /> <span>HECHO PARA TU RITMO</span></section>

        <section className="landing-how" id="como-funciona">
          <div className="landing-section-heading"><span className="eyebrow"><Compass size={14} /> EL MÉTODO STUDYIA</span><h2>De dónde estás a<br /><em>lo que sigue.</em></h2><p>No te damos una biblioteca infinita. Te ayudamos a tomar mejores decisiones sobre qué aprender ahora.</p></div>
          <div className="landing-steps"><article><span>01</span><Compass size={18} /><h3>Entendemos tu punto de partida</h3><p>Conocemos tu experiencia, tus intereses y el tiempo que realmente tienes.</p></article><article><span>02</span><ShieldCheck size={18} /><h3>Encontramos tus prioridades</h3><p>Un diagnóstico breve revela qué habilidades te darán más avance.</p></article><article><span>03</span><Sparkles size={18} /><h3>Construimos tu siguiente paso</h3><p>Recibes una ruta concreta, flexible y diseñada para mantenerte avanzando.</p></article></div>
        </section>
      </main>
      <footer><span>studyai / 2026</span><span>Hecho para aprender mejor, no más.</span><Link to="/login">Entrar <ArrowUpRight size={14} /></Link></footer>
    </div>
  )
}

function DashboardPage() {
  const [error, setError] = useState('')
  const [assessmentId, setAssessmentId] = useState<string | null>(null)
  const { user, logout: logoutSession } = useAuthStore()
  const navigate = useNavigate()

  async function handleProfileSubmit(profile: StudentProfile): Promise<AssessmentQuestion[]> {
    setError('')
    try {
      const response = await generateAssessment(profile)
      setAssessmentId(response.assessmentId)
      return response.questions
    } catch (error) {
      throw new Error(apiErrorMessage(error, 'No se pudo generar el diagnóstico.'))
    }
  }

  async function handleExamSubmit(answers: AssessmentAnswer[]) {
    if (!assessmentId) {
      setError('Falta la referencia del examen para evaluarlo.')
      return
    }

    try {
      await evaluateAssessment(assessmentId, answers)
      navigate('/roadmap')
    } catch (error) {
      throw new Error(apiErrorMessage(error, 'No pudimos evaluar tu examen en este momento.'))
    }
  }

  function logout() {
    void logoutSession()
  }

  return <div className="app-shell"><header className="topbar"><Link className="brand" to="/app"><img className="brand-logo" src="/logostudyia.png" alt="Study AI" /></Link><nav><span className="user-greeting">Hola, {user?.name.split(' ')[0]}</span><Link to="/progress">Mi progreso</Link><button className="logout-button" onClick={logout}><LogOut size={14} /> Salir</button></nav><button className="menu-button" aria-label="Abrir menú"><Menu size={20} /></button></header><main><section className="hero-layout" id="como-funciona"><div className="hero-copy"><div className="status-pill"><span /> Tu espacio de aprendizaje</div><h1>Aprende con <em>dirección.</em></h1><p className="hero-lede">Cuatro señales bastan para construir una ruta que se ajuste a tu punto de partida y a tu tiempo real.</p><div className="signal-list"><div><span className="signal-icon coral"><Compass size={17} /></span><p><strong>Desde donde estás</strong><small>Usamos lo que ya sabes como punto de partida.</small></p></div><div><span className="signal-icon blue"><ShieldCheck size={17} /></span><p><strong>Hacia donde quieres ir</strong><small>Traducimos tu objetivo en pasos concretos.</small></p></div><div><span className="signal-icon gold"><Sparkles size={17} /></span><p><strong>A tu propio ritmo</strong><small>Tu tiempo disponible marca el paso.</small></p></div></div></div><div>{error && <div className="notice">{error}</div>}<OnboardingFlow onProfileSubmit={handleProfileSubmit} onExamSubmit={handleExamSubmit} onError={setError} /></div></section></main><footer><span>studyai / 2026</span><span>Tu aprendizaje, con dirección.</span><button className="footer-logout" onClick={logout}>Cerrar sesión <ArrowUpRight size={14} /></button></footer></div>
}

function ProgressPage() {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null)
  const [error, setError] = useState('')
  const { user, logout } = useAuthStore()

  useEffect(() => {
    void getDashboard().then(setDashboard).catch((error) => setError(apiErrorMessage(error, 'No se pudo cargar tu progreso.')))
  }, [])

  if (!dashboard) {
    return <div className="progress-shell"><WorkspaceHeader userName={user?.name} onLogout={logout} /><main className="progress-loading">{error || 'Cargando tu espacio de progreso...'}</main></div>
  }

  return <div className="progress-shell">
    <WorkspaceHeader userName={user?.name} onLogout={logout} />
    <main className="progress-main">
      <div className="progress-welcome"><div><span className="dashboard-kicker">RESUMEN DE APRENDIZAJE</span><h1>Hola, {user?.name.split(' ')[0] ?? 'estudiante'}.</h1><p>Esto es lo que está pasando con tu ruta hoy.</p></div><Link className="progress-secondary-action" to="/roadmap">Ver roadmap <ArrowUpRight size={15} /></Link></div>
      <section className="progress-hero-card"><div className="active-route-copy"><span className="eyebrow"><Sparkles size={14} /> RUTA ACTIVA</span><h2>{dashboard.roadmap.title}</h2><p>{dashboard.roadmap.summary}</p><div className="active-route-progress"><div className="active-route-progress-label"><span>Progreso de la ruta</span><strong>{dashboard.metrics.progressPercentage}%</strong></div><div className="metric-bar"><i style={{ width: `${dashboard.metrics.progressPercentage}%` }} /></div></div><div className="progress-hero-foot"><span><CheckCircle2 size={14} /> {dashboard.metrics.completedActivities} de {dashboard.metrics.totalActivities} actividades completadas</span><span><Flame size={14} /> {dashboard.metrics.currentStreakDays} días de racha</span></div></div><div className="active-route-action"><div className="progress-ring" style={{ background: `conic-gradient(var(--primary) ${dashboard.metrics.progressPercentage}%, #eee8fb 0)` }}><strong>{dashboard.metrics.progressPercentage}%</strong><span>completado</span></div><Link className="primary-button" to="/roadmap">Continuar aprendiendo <ArrowRight size={15} /></Link></div></section>
      <section className="metrics-grid"><article><span>ACTIVIDADES COMPLETADAS</span><strong>{dashboard.metrics.completedActivities}<small> / {dashboard.metrics.totalActivities}</small></strong><p>de tu ruta</p></article><article><span>RACHA ACTUAL</span><strong>{dashboard.metrics.currentStreakDays}<small> días</small></strong><p>mantén el impulso</p></article></section>
      <div className="progress-section-heading"><div><span className="dashboard-kicker">DETALLE DE LA RUTA</span><h2>Organiza tu siguiente paso</h2></div><Link className="progress-inline-link" to="/roadmap">Abrir roadmap <ArrowRight size={14} /></Link></div>
    </main>
  </div>
}

function RoadmapPage() {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null)
  const [error, setError] = useState('')
  const { user, logout } = useAuthStore()

  useEffect(() => {
    void getDashboard().then(setDashboard).catch((error) => setError(apiErrorMessage(error, 'No se pudo cargar tu roadmap.')))
  }, [])

  if (!dashboard) {
    return <div className="roadmap-shell"><WorkspaceHeader userName={user?.name} onLogout={logout} /><main className="progress-loading">{error || 'Preparando tu roadmap...'}</main></div>
  }

  return <div className="roadmap-shell">
    <WorkspaceHeader userName={user?.name} onLogout={logout} />
    <main className="roadmap-main">
      <div className="roadmap-page-heading"><div><span className="dashboard-kicker">TU PLAN PERSONALIZADO</span><h1>{dashboard.roadmap.title}</h1><p>{dashboard.roadmap.summary}</p>{dashboard.roadmap.level && <span className="roadmap-level">Nivel {dashboard.roadmap.level === 'beginner' ? 'básico' : dashboard.roadmap.level === 'intermediate' ? 'intermedio' : 'avanzado'}</span>}</div><Link className="roadmap-progress-link" to="/progress">Ver mi progreso <ArrowRight size={15} /></Link></div>
      <div className="roadmap-overview"><span><CalendarDays size={15} /> {dashboard.roadmap.durationMonths ?? dashboard.metrics.totalStages} meses</span><span><Layers3 size={15} /> {dashboard.metrics.totalStages} etapas mensuales</span><span><CheckCircle2 size={15} /> {dashboard.metrics.totalActivities} actividades</span></div>
      <RoadmapTimeline dashboard={dashboard} onComplete={async (activityId) => setDashboard(await completeActivity(activityId))} />
    </main>
  </div>
}

export default function App() {
  const hydrate = useAuthStore((state) => state.hydrate)
  useEffect(() => { void hydrate() }, [hydrate])

  return <Routes>
    <Route path="/" element={<PublicHome />} />
    <Route element={<AuthPage />}><Route path="/login" element={<LoginForm />} /><Route path="/register" element={<RegisterForm />} /><Route path="/verify-email" element={<VerifyEmailForm />} /><Route path="/forgot-password" element={<ForgotPasswordForm />} /><Route path="/reset-password" element={<ResetPasswordForm />} /></Route>
    <Route element={<ProtectedRoute />}><Route path="/app" element={<DashboardPage />} /><Route path="/roadmap" element={<RoadmapPage />} /><Route path="/progress" element={<ProgressPage />} /><Route path="/profile" element={<ProfilePage />} /></Route>
  </Routes>
}
