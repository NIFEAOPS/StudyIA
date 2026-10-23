import { useState, type FormEvent } from 'react'
import { ArrowRight, Check, Clock3, Sparkles, Target } from 'lucide-react'
import { useAuthStore } from '../auth/authStore'
import { PathSelector } from './PathSelector'
import type { StudentProfile } from '../types'

type ProfileFormProps = {
  onSubmit: (profile: StudentProfile) => void | Promise<void>
  isSubmitting: boolean
}

const initialProfile: Omit<StudentProfile, 'name' | 'email'> = {
  learningPath: '',
  weeklyAvailabilityHours: 4,
  durationMonths: 3,
  professionalGoals: '',
}

export function ProfileForm({ onSubmit, isSubmitting }: ProfileFormProps) {
  const [profile, setProfile] = useState(initialProfile)
  const userName = useAuthStore((state) => state.user?.name?.trim()?.split(/\s+/)[0] ?? 'estudiante')

  function updateProfile<K extends keyof Omit<StudentProfile, 'name' | 'email'>>(key: K, value: Omit<StudentProfile, 'name' | 'email'>[K]) {
    setProfile((current) => ({ ...current, [key]: value }))
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    void Promise.resolve(onSubmit({
      learningPath: profile.learningPath,
      weeklyAvailabilityHours: profile.weeklyAvailabilityHours,
      durationMonths: profile.durationMonths,
      professionalGoals: profile.professionalGoals,
    }))
  }

  return (
    <form className="profile-form" onSubmit={handleSubmit}>
      <div className="form-intro">
        <span className="eyebrow"><Sparkles size={14} /> Perfil de aprendizaje</span>
        <h2>Hola, {userName}. Cuéntanos hacia dónde vas.</h2>
        <p>Con cuatro señales, Study AI arma una ruta que se ajusta a tu punto de partida y a tu tiempo real.</p>
      </div>

      <fieldset className="field-group">
        <div className="field-label-row"><div><label>¿Qué camino quieres seguir?</label><span>La ruta orienta el examen y el roadmap</span></div></div>
        <PathSelector value={profile.learningPath} onChange={(value) => updateProfile('learningPath', value)} />
      </fieldset>

      <div className="availability-row">
        <div className="availability-copy"><Clock3 size={19} /><div><label htmlFor="hours">Tiempo disponible</label><span>Entre 2 y 8 horas de estudio por semana</span></div></div>
        <div className="hours-control"><input id="hours" type="range" min="2" max="8" value={profile.weeklyAvailabilityHours} onChange={(event) => updateProfile('weeklyAvailabilityHours', Number(event.target.value))} /><strong>{profile.weeklyAvailabilityHours} h</strong></div>
      </div>

      <label className="text-field goals-field">
        <span>¿Cuántos meses quieres dedicarle?</span>
        <select
          required
          value={profile.durationMonths}
          onChange={(event) => updateProfile('durationMonths', Number(event.target.value))}
        >
          <option value={3}>3 meses</option>
          <option value={6}>6 meses</option>
          <option value={9}>9 meses</option>
          <option value={12}>12 meses</option>
        </select>
      </label>

      <label className="text-field goals-field">
        <span>¿Cuál es tu objetivo profesional?</span>
        <div className="input-with-icon textarea-wrap"><Target size={17} /><textarea required value={profile.professionalGoals} onChange={(event) => updateProfile('professionalGoals', event.target.value)} placeholder="Ej. Conseguir mi primera posición como analista SOC..." rows={3} /></div>
      </label>

      <button className="primary-button" type="submit" disabled={isSubmitting || !profile.learningPath}>
        {isSubmitting ? 'Analizando tu perfil...' : 'Continuar con mi diagnóstico'}
        {isSubmitting ? <span className="button-loader" /> : <ArrowRight size={18} />}
      </button>
      <p className="form-note"><Check size={14} /> Tu perfil se usa solo para personalizar esta ruta.</p>
    </form>
  )
}
