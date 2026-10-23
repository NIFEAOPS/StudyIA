import { Check } from 'lucide-react'

type PathSelectorProps = {
  value: string
  onChange: (value: string) => void
}

const learningPaths = [
  { id: 'frontend', title: 'Desarrollo frontend', description: 'Interfaces web, React y experiencias digitales' },
  { id: 'backend', title: 'Desarrollo backend', description: 'APIs, servidores, bases de datos y arquitectura' },
  { id: 'fullstack', title: 'Desarrollo full stack', description: 'Construye productos completos de punta a punta' },
  { id: 'mobile', title: 'Desarrollo móvil', description: 'Apps para Android, iOS y multiplataforma' },
  { id: 'cloud-devops', title: 'Cloud y DevOps', description: 'AWS, Azure, GCP, CI/CD, Docker y Kubernetes' },
  { id: 'data-ai', title: 'Datos e inteligencia artificial', description: 'Python, análisis de datos, ML y automatización' },
  { id: 'data-engineering', title: 'Ingeniería de datos', description: 'Pipelines, data warehouses y plataformas de datos' },
  { id: 'cybersecurity', title: 'Ciberseguridad', description: 'Redes, sistemas, defensa y análisis de amenazas' },
  { id: 'qa-testing', title: 'QA y testing', description: 'Calidad, pruebas automatizadas y testing de APIs' },
  { id: 'ux-ui', title: 'UX/UI y diseño digital', description: 'Investigación, experiencia de usuario e interfaces' },
  { id: 'blockchain', title: 'Blockchain y Web3', description: 'Smart contracts, criptografía y aplicaciones descentralizadas' },
  { id: 'product-management', title: 'Producto y gestión tecnológica', description: 'Discovery, estrategia, proyectos y liderazgo tech' },
]

export function PathSelector({ value, onChange }: PathSelectorProps) {
  return (
    <div className="path-selector" role="radiogroup" aria-label="Selector de ruta de aprendizaje">
      {learningPaths.map((path) => {
        const isSelected = value === path.id

        return (
          <button
            key={path.id}
            type="button"
            className={`path-option${isSelected ? ' selected' : ''}`}
            role="radio"
            aria-checked={isSelected}
            onClick={() => onChange(path.id)}
          >
            <span className="path-option-copy">
              <strong>{path.title}</strong>
              <small>{path.description}</small>
            </span>
            <span className="path-check" aria-hidden="true">
              <Check size={12} />
            </span>
          </button>
        )
      })}
    </div>
  )
}
