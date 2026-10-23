import { motion } from 'framer-motion'
import { ArrowRight, BrainCircuit, BriefcaseBusiness, CheckCircle2, Compass, GraduationCap, Sparkles, Target } from 'lucide-react'
import { Link } from 'react-router-dom'

export function HeroOnboarding() {
  return (
    <section className="relative overflow-hidden bg-[#0a0a0a] text-white">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(94,231,255,0.18),_transparent_30%),radial-gradient(circle_at_top_right,_rgba(255,107,87,0.15),_transparent_24%)]" />
      <div className="grid-overlay absolute inset-0 opacity-40" />

      <div className="relative mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8 lg:pb-28 lg:pt-16">
        <motion.div
          initial={{ opacity: 0, y: 18 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.45, ease: 'easeOut' }}
          className="mx-auto max-w-3xl text-center"
        >
          <span className="inline-flex items-center gap-2 rounded-full border border-cyan-400/30 bg-cyan-400/10 px-3 py-1 text-[10px] font-medium uppercase tracking-[0.28em] text-cyan-200/90 shadow-[0_0_15px_rgba(94,231,255,0.2)]">
            <BrainCircuit className="h-3.5 w-3.5" />
            IA learning system
          </span>

          <h1 className="mt-6 text-4xl font-black tracking-[-0.08em] text-white sm:text-5xl lg:text-7xl">
            Aprende con <span className="text-gradient">dirección</span>
          </h1>

          <p className="mx-auto mt-5 max-w-xl text-sm text-zinc-300 sm:text-base">
            Diseñamos una ruta clara, inteligente y adaptada a tu nivel real para que avances con precisión y sin ruido.
          </p>

          <div className="mt-8 flex flex-col items-center justify-center gap-3 sm:flex-row">
            <Link to="/register" className="magnetic inline-flex items-center gap-2 rounded-full border border-white/10 bg-white px-5 py-3 text-sm font-semibold text-[#0a0a0a] shadow-[0_20px_40px_rgba(255,255,255,0.15)] transition hover:bg-[#f2f5f7]">
              Construir mi ruta
              <ArrowRight className="h-4 w-4" />
            </Link>
            <a href="#como-funciona" className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-5 py-3 text-sm font-medium text-zinc-100 backdrop-blur-sm transition hover:border-cyan-400/50 hover:bg-cyan-400/10">
              <Sparkles className="h-4 w-4 text-cyan-300" />
              Cómo funciona
            </a>
          </div>
        </motion.div>

        <div className="home-value-grid">
          <div className="home-value-intro">
            <span className="eyebrow"><Compass className="h-3.5 w-3.5" /> La idea detrás de StudyIA</span>
            <h2>No te damos más contenido. Te damos el siguiente paso correcto.</h2>
            <p>StudyIA convierte tu punto de partida, tus objetivos y el tiempo que realmente tienes en una ruta que puedes seguir.</p>
          </div>
          <div className="home-value-steps">
            <article><span><Target className="h-4 w-4" /></span><div><strong>1. Entendemos tu contexto</strong><p>Perfil, intereses, experiencia y meta profesional.</p></div></article>
            <article><span><BrainCircuit className="h-4 w-4" /></span><div><strong>2. Medimos tus habilidades</strong><p>Un diagnóstico breve que se adapta a cómo piensas y resuelves.</p></div></article>
            <article><span><CheckCircle2 className="h-4 w-4" /></span><div><strong>3. Ordenamos tu aprendizaje</strong><p>Una ruta priorizada con objetivos, semanas y acciones concretas.</p></div></article>
          </div>
        </div>

        <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 0.08 }} className="home-audience-grid" id="como-funciona">
          <article className="home-audience-card home-audience-card-featured"><div className="home-audience-icon"><GraduationCap className="h-5 w-5" /></div><span className="home-audience-label">Para quienes empiezan</span><h2>Convierte la curiosidad en una habilidad real.</h2><p>Descubre qué aprender primero, entiende por qué importa y avanza con una ruta que no te abruma.</p><Link to="/register">Encuentra tu punto de partida <ArrowRight className="h-4 w-4" /></Link></article>
          <article className="home-audience-card"><div className="home-audience-icon"><BriefcaseBusiness className="h-5 w-5" /></div><span className="home-audience-label">Para quienes evolucionan</span><h2>Haz que tu experiencia también tenga dirección.</h2><p>Adquiere nuevas habilidades, cambia de especialidad o fortalece lo que ya haces profesionalmente.</p><Link to="/register">Diseña tu siguiente etapa <ArrowRight className="h-4 w-4" /></Link></article>
          <div className="home-promise"><div><Target className="h-5 w-5 text-cyan-300" /><strong>Una ruta pensada para tu realidad</strong></div><p>No importa si estudias, trabajas o estás cambiando de camino. StudyIA parte de ti y te muestra qué sigue.</p><span><CheckCircle2 className="h-4 w-4" /> Menos ruido. Más avance.</span></div>
        </motion.div>
      </div>
    </section>
  )
}
