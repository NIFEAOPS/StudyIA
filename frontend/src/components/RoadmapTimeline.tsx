import { ArrowLeft, ArrowRight, CheckCircle2, Circle, ExternalLink, LockKeyhole } from 'lucide-react'
import { useState } from 'react'
import type { Dashboard } from '../types'

type RoadmapTimelineProps = { dashboard: Dashboard; onComplete: (activityId: string) => Promise<void> }

export function RoadmapTimeline({ dashboard, onComplete }: RoadmapTimelineProps) {
  const [activeMonth, setActiveMonth] = useState(0)
  const activeStage = dashboard.stages[activeMonth]

  return (
    <section className="roadmap-panel">
      {dashboard.stages.length === 0 ? <div className="empty-roadmap"><p>Aún no hay etapas disponibles para mostrar.</p></div> : (
        <div className="weekly-roadmap">
          <div className="week-pager">
            <button className="week-pager-button" onClick={() => setActiveMonth((month) => Math.max(0, month - 1))} disabled={activeMonth === 0} aria-label="Mes anterior"><ArrowLeft size={17} /></button>
            <div className="week-pager-label"><span>MES {activeStage.monthNumber ?? activeStage.stageNumber}</span><strong>{activeMonth + 1} <small>/ {dashboard.stages.length}</small></strong></div>
            <button className="week-pager-button" onClick={() => setActiveMonth((month) => Math.min(dashboard.stages.length - 1, month + 1))} disabled={activeMonth === dashboard.stages.length - 1} aria-label="Siguiente mes"><ArrowRight size={17} /></button>
          </div>
          <div className="week-progress-track" aria-label="Progreso por meses">
            {dashboard.stages.map((stage, index) => <button key={stage.id} className={`week-progress-dot ${index === activeMonth ? 'current' : ''} ${stage.status === 'COMPLETED' ? 'done' : ''} ${stage.status === 'LOCKED' ? 'locked' : ''}`} onClick={() => setActiveMonth(index)} aria-label={`Ir a mes ${index + 1}`}><span /></button>)}
          </div>
          <article className="weekly-card">
            <div className="weekly-card-intro"><div className={`weekly-status status-${activeStage.status.toLowerCase()}`}>{activeStage.status === 'COMPLETED' ? <CheckCircle2 size={16} /> : activeStage.status === 'LOCKED' ? <LockKeyhole size={16} /> : <Circle size={16} />} {activeStage.status === 'COMPLETED' ? 'Mes completado' : activeStage.status === 'IN_PROGRESS' ? 'En curso' : activeStage.status === 'LOCKED' ? 'Bloqueado' : 'Disponible'}</div><h3>{activeStage.title}</h3><p>{activeStage.objective}</p></div>
            <div className="weekly-activities"><div className="weekly-activities-heading"><span>ACTIVIDADES</span><strong>{activeStage.activities.filter((activity) => activity.status === 'COMPLETED').length} / {activeStage.activities.length}</strong></div>
              {activeStage.activities.map((activity) => <div className={`weekly-activity ${activity.status === 'COMPLETED' ? 'completed' : ''} ${activeStage.status === 'LOCKED' ? 'locked' : ''}`} key={activity.id}><label className="weekly-activity-main"><input type="checkbox" checked={activity.status === 'COMPLETED'} disabled={activeStage.status === 'LOCKED'} onChange={() => { if (activity.status !== 'COMPLETED' && activeStage.status !== 'LOCKED') void onComplete(activity.id) }} /><span className="weekly-activity-check">{activity.status === 'COMPLETED' ? <CheckCircle2 size={16} /> : activeStage.status === 'LOCKED' ? <LockKeyhole size={14} /> : <Circle size={16} />}</span><span className="weekly-activity-copy"><strong>{activity.title}</strong>{activity.description && <small>{activity.description}</small>}</span></label>{activity.resourceUrl && <a className="weekly-resource-link" href={activity.resourceUrl} target="_blank" rel="noreferrer"><ExternalLink size={14} /> Ver recurso</a>}{activity.estimatedMinutes && <small className="weekly-activity-time">{activity.estimatedMinutes} min</small>}</div>)}
              {activeStage.resources.length > 0 && <div className="weekly-resources"><div className="weekly-activities-heading"><span>RECURSOS PARA ESTE MES</span><strong>{activeStage.resources.length}</strong></div>{activeStage.resources.map((resource) => <a className="weekly-resource-card" key={resource.id ?? resource.url} href={resource.url} target="_blank" rel="noreferrer"><span className="weekly-resource-type">{resource.type ?? 'RECURSO'}</span><span><strong>{resource.title}</strong><small>{resource.description}</small></span><ExternalLink size={15} /></a>)}</div>}
            </div>
          </article>
        </div>
      )}
    </section>
  )
}
