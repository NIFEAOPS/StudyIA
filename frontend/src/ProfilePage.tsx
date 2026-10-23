import { useEffect, useState } from 'react'
import { Check, CheckCircle2, Eye, EyeOff, KeyRound, Mail, Phone, ShieldCheck, UserRound } from 'lucide-react'
import { Link } from 'react-router-dom'
import { changePassword, getProfile, updateProfile } from './api'
import { apiErrorMessage } from './apiClient'
import { useAuthStore } from './auth/authStore'
import type { Profile } from './types'
import { WorkspaceHeader } from './WorkspaceHeader'

export function ProfilePage() {
  const [profile, setProfile] = useState<Profile | null>(null)
  const [university, setUniversity] = useState('')
  const [phone, setPhone] = useState('')
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [visiblePasswords, setVisiblePasswords] = useState<Record<string, boolean>>({})
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [savingProfile, setSavingProfile] = useState(false)
  const [savingPassword, setSavingPassword] = useState(false)
  const setSession = useAuthStore((state) => state.setSession)
  const { user, logout } = useAuthStore()
  const passwordRequirements = [
    { label: 'Al menos 8 caracteres', valid: newPassword.length >= 8 },
    { label: 'Una letra mayúscula y una minúscula', valid: /[A-Z]/.test(newPassword) && /[a-z]/.test(newPassword) },
    { label: 'Un número y un símbolo', valid: /[0-9]/.test(newPassword) && /[^A-Za-z0-9]/.test(newPassword) },
  ]

  function togglePassword(field: string) {
    setVisiblePasswords((current) => ({ ...current, [field]: !current[field] }))
  }

  useEffect(() => {
    void getProfile().then((data) => {
      setProfile(data)
      setUniversity(data.university ?? '')
      setPhone(data.phone ?? '')
    }).catch((requestError) => {
      setError(apiErrorMessage(requestError, 'No se pudo cargar tu perfil.'))
    })
  }, [])

  async function saveProfile(event: React.FormEvent) {
    event.preventDefault()
    setSavingProfile(true); setMessage(''); setError('')
    try {
      const updated = await updateProfile(university, phone)
      setProfile(updated)
      setMessage('Datos actualizados correctamente.')
      const session = useAuthStore.getState()
      if (session.user) setSession({ accessToken: session.accessToken ?? '', tokenType: 'Bearer', expiresIn: 0, ...updated })
    } catch (requestError) {
      setError(apiErrorMessage(requestError, 'No se pudieron actualizar tus datos.'))
    } finally { setSavingProfile(false) }
  }

  async function savePassword(event: React.FormEvent) {
    event.preventDefault()
    if (newPassword !== confirmPassword) {
      setError('Las contraseñas nuevas no coinciden.')
      setMessage('')
      return
    }
    if (passwordRequirements.some((requirement) => !requirement.valid)) {
      setError('La nueva contraseña no cumple todos los requisitos de seguridad.')
      setMessage('')
      return
    }
    setSavingPassword(true); setMessage(''); setError('')
    try {
      await changePassword(currentPassword, newPassword)
      setCurrentPassword(''); setNewPassword(''); setConfirmPassword('')
      setMessage('Contraseña actualizada. Tu sesión actual sigue activa.')
    } catch (requestError) {
      setError(apiErrorMessage(requestError, 'No se pudo cambiar la contraseña.'))
    } finally { setSavingPassword(false) }
  }

  return <div className="progress-shell settings-shell"><WorkspaceHeader userName={user?.name} onLogout={logout} /><main className="settings-page">
    <div className="settings-heading"><div><Link className="settings-back" to="/progress">← Mi progreso</Link><h1>Tu perfil</h1><p>Administra tus datos y mantén segura tu cuenta.</p></div><div className="settings-heading-meta"><span className="settings-status"><CheckCircle2 size={14} /> Correo verificado</span><Link className="progress-secondary-action" to="/progress">Cancelar</Link></div></div>
    {error && <div className="notice">{error}</div>}
    {message && <div className="success-notice"><CheckCircle2 size={16} /> {message}</div>}
    <div className="settings-grid">
      <form className="settings-card" onSubmit={(event) => void saveProfile(event)}>
        <div className="settings-card-heading"><span className="settings-icon"><UserRound size={18} /></span><div><h2>Datos personales</h2><p>Los datos de tu cuenta se mantienen protegidos.</p></div></div>
        <label className="text-field"><span>Nombre</span><div className="read-only-field"><UserRound size={16} />{profile?.name ?? 'Cargando...'}</div></label>
        <label className="text-field"><span>Correo verificado</span><div className="read-only-field"><Mail size={16} />{profile?.email ?? 'Cargando...' }<CheckCircle2 className="read-only-status" size={16} /></div></label>
        <label className="text-field"><span>Universidad</span><input value={university} onChange={(event) => setUniversity(event.target.value)} placeholder="Ej. Universidad Nacional" maxLength={160} /></label>
        <label className="text-field"><span>Número de teléfono <small>(opcional)</small></span><div className="settings-input-with-icon"><Phone size={16} /><input type="tel" value={phone} onChange={(event) => setPhone(event.target.value)} placeholder="+57 300 000 0000" maxLength={30} autoComplete="tel" /></div></label>
        <button className="primary-button" type="submit" disabled={savingProfile}>{savingProfile ? 'Guardando...' : 'Guardar cambios'}</button>
      </form>
      <form className="settings-card security-card" onSubmit={(event) => void savePassword(event)}>
        <div className="settings-card-heading"><span className="settings-icon"><KeyRound size={18} /></span><div><h2>Cambiar contraseña</h2><p>Usa una contraseña única y difícil de adivinar.</p></div></div>
        <PasswordField id="current-password" label="Contraseña actual" placeholder="Ingresa tu contraseña actual" value={currentPassword} onChange={setCurrentPassword} visible={Boolean(visiblePasswords.current)} onToggle={() => togglePassword('current')} autoComplete="current-password" />
        <PasswordField id="new-password" label="Nueva contraseña" placeholder="Ingresa tu nueva contraseña" value={newPassword} onChange={setNewPassword} visible={Boolean(visiblePasswords.new)} onToggle={() => togglePassword('new')} autoComplete="new-password" />
        <div className="password-requirements" aria-live="polite">{passwordRequirements.map((requirement) => <span className={requirement.valid ? 'valid' : ''} key={requirement.label}><Check size={13} /> {requirement.label}</span>)}</div>
        <PasswordField id="confirm-password" label="Confirmar contraseña" placeholder="Confirma tu nueva contraseña" value={confirmPassword} onChange={setConfirmPassword} visible={Boolean(visiblePasswords.confirm)} onToggle={() => togglePassword('confirm')} autoComplete="new-password" error={confirmPassword.length > 0 && confirmPassword !== newPassword ? 'Las contraseñas no coinciden' : undefined} />
        <div className="settings-actions"><Link className="settings-cancel" to="/progress">Cancelar</Link><button className="primary-button" type="submit" disabled={savingPassword}>{savingPassword ? 'Actualizando...' : 'Actualizar contraseña'}</button></div>
      </form>
    </div>
    <div className="settings-support-row"><section className="quota-card"><div><span className="eyebrow">USO DE LA CUENTA</span><h2>Roadmaps creados</h2><p>Tu límite personal de rutas.</p></div><strong>{profile?.roadmapCount ?? 0} <small>/ {profile?.roadmapLimit ?? 3}</small></strong></section><aside className="security-tips"><div className="settings-icon"><ShieldCheck size={18} /></div><div><h2>Consejos de seguridad</h2><p>No reutilices contraseñas ni compartas tus credenciales.</p></div></aside>    </div></main>
  </div>
}

type PasswordFieldProps = {
  id: string
  label: string
  placeholder: string
  value: string
  onChange: (value: string) => void
  visible: boolean
  onToggle: () => void
  autoComplete: string
  error?: string
}

function PasswordField({ id, label, placeholder, value, onChange, visible, onToggle, autoComplete, error }: PasswordFieldProps) {
  return <label className="text-field password-field" htmlFor={id}><span>{label}</span><div className="password-input-wrap"><input id={id} type={visible ? 'text' : 'password'} placeholder={placeholder} value={value} onChange={(event) => onChange(event.target.value)} required autoComplete={autoComplete} /><button type="button" onClick={onToggle} aria-label={visible ? `Ocultar ${label.toLowerCase()}` : `Mostrar ${label.toLowerCase()}`}>{visible ? <EyeOff size={17} /> : <Eye size={17} />}</button></div>{error && <small className="field-inline-error">{error}</small>}</label>
}
