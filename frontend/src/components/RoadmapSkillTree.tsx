import { motion } from 'framer-motion'
import { Check, Lock, Sparkles } from 'lucide-react'

const weeks = [
  { id: 1, label: 'Semana 1', title: 'Fundamentos', status: 'done', topics: ['Python básico', 'Lógica de programación'], accent: 'from-cyan-400 to-cyan-600' },
  { id: 2, label: 'Semana 2', title: 'Redes y protocolos', status: 'active', topics: ['TCP/IP', 'DNS', 'Firewall'], accent: 'from-coral to-orange-500' },
  { id: 3, label: 'Semana 3', title: 'Sistemas seguros', status: 'locked', topics: ['Autenticación', 'IAM', 'Seguridad'], accent: 'from-violet-500 to-indigo-500' },
  { id: 4, label: 'Semana 4', title: 'Práctica ofensiva', status: 'locked', topics: ['Escaneo', 'Enumeración', 'Hardening'], accent: 'from-gold to-yellow-500' },
]

export function RoadmapSkillTree() {
  return (
    <div className="relative mx-auto w-full max-w-6xl overflow-hidden rounded-[28px] border border-white/10 bg-[#111111]/80 p-4 shadow-panel backdrop-blur-xl sm:p-6">
      <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-[10px] uppercase tracking-[0.24em] text-cyan-300/80">Roadmap principal</p>
          <h3 className="mt-2 text-2xl font-black tracking-[-0.06em] text-white sm:text-3xl">Skill tree de seguridad</h3>
        </div>
        <div className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-3 py-2 text-xs text-zinc-200">
          <Sparkles className="h-3.5 w-3.5 text-cyan-300" />
          72% de progreso
        </div>
      </div>

      <div className="relative">
        <svg viewBox="0 0 900 260" className="pointer-events-none absolute inset-0 h-full w-full opacity-80" aria-hidden="true">
          <path d="M80 120 C220 70, 320 140, 430 120 S660 70, 820 130" fill="none" stroke="url(#lineGlow)" strokeWidth="3" strokeLinecap="round" />
          <defs>
            <linearGradient id="lineGlow" x1="0%" x2="100%" y1="50%" y2="50%">
              <stop offset="0%" stopColor="#5EE7FF" stopOpacity="0.55" />
              <stop offset="50%" stopColor="#FF6B57" stopOpacity="0.9" />
              <stop offset="100%" stopColor="#F4C75E" stopOpacity="0.7" />
            </linearGradient>
          </defs>
        </svg>

        <div className="relative grid gap-4 md:grid-cols-4">
          {weeks.map((week, index) => {
            const isActive = week.status === 'active'
            const isDone = week.status === 'done'
            const isLocked = week.status === 'locked'

            return (
              <motion.div
                key={week.id}
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.08, duration: 0.38 }}
                className={`relative rounded-[24px] border p-4 ${
                  isActive
                    ? 'border-coral/50 bg-coral/10 shadow-[0_0_18px_rgba(255,107,87,0.18)]'
                    : isDone
                      ? 'border-cyan-400/40 bg-cyan-400/10 shadow-[0_0_18px_rgba(94,231,255,0.15)]'
                      : 'border-white/10 bg-white/5 opacity-80'
                }`}
              >
                <div className={`mb-4 flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-br ${week.accent} text-sm font-black text-white shadow-[0_0_15px_rgba(255,255,255,0.1)]`}>
                  {isLocked ? <Lock className="h-4 w-4" /> : isDone ? <Check className="h-4 w-4" /> : week.id}
                </div>

                <p className="text-[10px] uppercase tracking-[0.2em] text-zinc-400">{week.label}</p>
                <h4 className="mt-3 text-lg font-bold text-white">{week.title}</h4>

                <ul className="mt-3 space-y-2">
                  {week.topics.map((topic) => (
                    <li key={topic} className="flex items-center gap-2 text-sm text-zinc-300">
                      <span className="h-1.5 w-1.5 rounded-full bg-cyan-300" />
                      {topic}
                    </li>
                  ))}
                </ul>

                <span
                  className={`mt-5 inline-flex items-center gap-2 rounded-full px-3 py-2 text-xs font-medium transition ${
                    isLocked
                      ? 'border border-white/10 bg-white/5 text-zinc-300'
                      : 'bg-white text-[#0a0a0a] shadow-[0_0_18px_rgba(255,255,255,0.12)]'
                  }`}
                >
                  {isLocked ? 'Bloqueado' : isDone ? 'Revisado' : 'Continuar'}
                </span>
              </motion.div>
            )
          })}
        </div>
      </div>
    </div>
  )
}
