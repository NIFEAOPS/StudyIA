import { useState } from 'react'
import { Sparkles } from 'lucide-react'
import { AssessmentQuiz } from './AssessmentQuiz'
import { ProfileForm } from './ProfileForm'
import type { AssessmentAnswer, AssessmentQuestion, StudentProfile } from '../types'

type OnboardingStep = 'STEP_PROFILE' | 'STEP_LOADING_EXAM' | 'STEP_EXAM' | 'STEP_LOADING_ROADMAP'

const defaultExamQuestions: AssessmentQuestion[] = [
  {
    id: 1,
    text: '¿Qué tan cómodo te sientes resolviendo problemas prácticos con tecnología?',
    options: [
      { id: 'A', label: 'A', text: 'Muy cómodo, ya lo hago en proyectos' },
      { id: 'B', label: 'B', text: 'Algo cómodo, pero aún necesito guía' },
      { id: 'C', label: 'C', text: 'Todavía estoy empezando' },
    ],
  },
  {
    id: 2,
    text: '¿Qué prefieres cuando quieres aprender algo nuevo?',
    options: [
      { id: 'A', label: 'A', text: 'Aplicarlo de inmediato en un caso real' },
      { id: 'B', label: 'B', text: 'Entender primero la lógica y la estructura' },
      { id: 'C', label: 'C', text: 'Seguir instrucciones paso a paso' },
    ],
  },
  {
    id: 3,
    text: '¿Qué tan constante puedes ser con tus sesiones de estudio?',
    options: [
      { id: 'A', label: 'A', text: 'Puedo mantener una frecuencia alta' },
      { id: 'B', label: 'B', text: 'Puedo hacerlo de forma regular, aunque no diaria' },
      { id: 'C', label: 'C', text: 'Necesito una planificación muy flexible' },
    ],
  },
]

type OnboardingFlowProps = {
  onProfileSubmit: (profile: StudentProfile) => Promise<AssessmentQuestion[] | void> | AssessmentQuestion[] | void
  onExamSubmit: (answers: AssessmentAnswer[]) => Promise<void> | void
  onError?: (message: string) => void
}

export function OnboardingFlow({ onProfileSubmit, onExamSubmit, onError }: OnboardingFlowProps) {
  const [step, setStep] = useState<OnboardingStep>('STEP_PROFILE')
  const [questions, setQuestions] = useState<AssessmentQuestion[]>(defaultExamQuestions)
  const [isSubmittingProfile, setIsSubmittingProfile] = useState(false)
  const [isSubmittingExam, setIsSubmittingExam] = useState(false)

  async function handleProfileSubmit(profile: StudentProfile) {
    setIsSubmittingProfile(true)
    setStep('STEP_LOADING_EXAM')

    try {
      const nextQuestions = await Promise.resolve(onProfileSubmit(profile))
      setQuestions(nextQuestions && nextQuestions.length > 0 ? nextQuestions : defaultExamQuestions)
      setStep('STEP_EXAM')
    } catch (error) {
      setStep('STEP_PROFILE')
      onError?.(error instanceof Error ? error.message : 'No se pudo generar el diagnóstico.')
    } finally {
      setIsSubmittingProfile(false)
    }
  }

  async function handleExamSubmit(answers: AssessmentAnswer[]) {
    setIsSubmittingExam(true)
    setStep('STEP_LOADING_ROADMAP')

    try {
      await Promise.resolve(onExamSubmit(answers))
    } catch (error) {
      setStep('STEP_EXAM')
      onError?.(error instanceof Error ? error.message : 'No se pudo generar el roadmap.')
    } finally {
      setIsSubmittingExam(false)
    }
  }

  if (step === 'STEP_PROFILE') {
    return <ProfileForm onSubmit={handleProfileSubmit} isSubmitting={isSubmittingProfile} />
  }

  if (step === 'STEP_LOADING_EXAM') {
    return (
      <div className="profile-form">
        <div className="form-intro">
          <span className="eyebrow"><Sparkles size={14} /> Diagnóstico</span>
          <h2>Analizando tu perfil para generar tu examen de diagnóstico...</h2>
        </div>
        <div style={{ display: 'grid', gap: '12px' }}>
          <div style={{ height: '12px', borderRadius: '999px', background: '#eceae3', overflow: 'hidden' }}>
            <div style={{ width: '76%', height: '100%', background: 'linear-gradient(90deg, #df654d, #f2c65e)', borderRadius: 'inherit' }} />
          </div>
          <p style={{ margin: 0, color: '#6f7670', fontSize: '12px', lineHeight: 1.6 }}>
            Estamos adaptando tus intereses, tiempo disponible y objetivos para preparar la evaluación más útil.
          </p>
        </div>
      </div>
    )
  }

  if (step === 'STEP_LOADING_ROADMAP') {
    return (
      <div className="profile-form">
        <div className="form-intro">
          <span className="eyebrow"><Sparkles size={14} /> Ruta personalizada</span>
          <h2>Estructurando tu ruta de aprendizaje basada en tus resultados...</h2>
        </div>
        <div style={{ display: 'grid', gap: '12px' }}>
          <div style={{ height: '12px', borderRadius: '999px', background: '#eceae3', overflow: 'hidden' }}>
            <div style={{ width: '88%', height: '100%', background: 'linear-gradient(90deg, #202321, #df654d)', borderRadius: 'inherit' }} />
          </div>
          <p style={{ margin: 0, color: '#6f7670', fontSize: '12px', lineHeight: 1.6 }}>
            Estamos convirtiendo tus respuestas en una hoja de ruta con prioridad, objetivos y temporización realista.
          </p>
        </div>
      </div>
    )
  }

  return <AssessmentQuiz questions={questions} onSubmit={handleExamSubmit} isSubmitting={isSubmittingExam} />
}
