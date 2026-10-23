import { useState } from 'react'
import { ArrowLeft, ArrowRight, Check, CheckCircle2 } from 'lucide-react'
import type { AssessmentAnswer, AssessmentQuestion } from '../types'

type AssessmentQuizProps = {
  questions: AssessmentQuestion[]
  onSubmit: (answers: AssessmentAnswer[]) => void | Promise<void>
  isSubmitting?: boolean
}

export function AssessmentQuiz({ questions, onSubmit, isSubmitting = false }: AssessmentQuizProps) {
  const [selectedOptions, setSelectedOptions] = useState<Record<number, string>>({})
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0)

  const allAnswered = questions.length > 0 && questions.every((question) => selectedOptions[question.id])
  const currentQuestion = questions[currentQuestionIndex]
  const isLastQuestion = currentQuestionIndex === questions.length - 1

  function handleOptionSelect(questionId: number, optionId: string) {
    setSelectedOptions((current) => ({ ...current, [questionId]: optionId }))
  }

  async function handleSubmit() {
    if (!allAnswered) return

    const answers: AssessmentAnswer[] = questions.map((question) => ({
      questionId: question.id,
      selectedOption: selectedOptions[question.id] ?? '',
    }))

    await Promise.resolve(onSubmit(answers))
  }

  function handleNext() {
    if (!currentQuestion || !selectedOptions[currentQuestion.id]) return
    if (isLastQuestion) {
      void handleSubmit()
      return
    }
    setCurrentQuestionIndex((current) => current + 1)
  }

  return (
    <div className="profile-form assessment-quiz">
      <div className="form-intro">
        <span className="eyebrow"><CheckCircle2 size={14} /> Diagnóstico</span>
        <h2>Evalúa tu nivel actual.</h2>
        <p>Una pregunta a la vez. No buscamos respuestas perfectas, sino entender cómo aprendes.</p>
      </div>

      <div className="assessment-progress" aria-label={`Pregunta ${currentQuestionIndex + 1} de ${questions.length}`}>
        <div className="assessment-progress-track"><span style={{ width: `${((currentQuestionIndex + 1) / questions.length) * 100}%` }} /></div>
        <span>{String(currentQuestionIndex + 1).padStart(2, '0')} / {String(questions.length).padStart(2, '0')}</span>
      </div>

      {currentQuestion && <fieldset className="assessment-question">
        <span className="assessment-question-index">Pregunta {currentQuestion.id}</span>
        <p className="assessment-question-text">{currentQuestion.text}</p>
        <div className="assessment-options">
          {currentQuestion.options.map((option) => {
            const isSelected = selectedOptions[currentQuestion.id] === option.id
            const optionLabel = option.label ?? option.id

            return <button key={option.id} type="button" className={`assessment-option${isSelected ? ' selected' : ''}`} onClick={() => handleOptionSelect(currentQuestion.id, option.id)}>
              <span className="assessment-option-badge">{optionLabel}</span>
              <span className="assessment-option-copy">{option.text}</span>
              <span className="assessment-option-check" aria-hidden="true"><Check size={12} /></span>
            </button>
          })}
        </div>
      </fieldset>}

      <div className="assessment-actions">
        <button type="button" className="assessment-back" onClick={() => setCurrentQuestionIndex((current) => Math.max(0, current - 1))} disabled={currentQuestionIndex === 0 || isSubmitting}>
          <ArrowLeft size={16} /> Atrás
        </button>
        <button type="button" className="primary-button" onClick={handleNext} disabled={!currentQuestion || !selectedOptions[currentQuestion.id] || isSubmitting}>
          {isSubmitting ? 'Evaluando...' : isLastQuestion ? 'Generar mi ruta' : 'Siguiente'}
          {!isSubmitting && <ArrowRight size={18} />}
        </button>
      </div>
    </div>
  )
}
