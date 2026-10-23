import type { Roadmap } from '../types'

export const demoRoadmap: Roadmap = {
  id: 'demo-roadmap',
  studentId: 'demo-student',
  title: 'Ruta hacia la ciberseguridad aplicada',
  summary: 'Una progresión de 8 semanas para convertir tus bases de sistemas en proyectos de seguridad demostrables.',
  structuredContent: JSON.stringify({
    weeks: [
      { week: 1, objective: 'Consolida tus fundamentos', topics: ['Linux y línea de comandos', 'Redes TCP/IP', 'Git y documentación técnica'] },
      { week: 2, objective: 'Aprende a observar una red', topics: ['Wireshark', 'DNS y HTTP', 'Modelo OSI'] },
      { week: 3, objective: 'Automatiza tareas de seguridad', topics: ['Python para scripting', 'Manejo de logs', 'APIs y autenticación'] },
      { week: 4, objective: 'Entiende las vulnerabilidades web', topics: ['OWASP Top 10', 'Burp Suite', 'Laboratorios guiados'] },
      { week: 5, objective: 'Construye tu laboratorio', topics: ['Docker', 'Máquinas virtuales', 'Segmentación de red'] },
      { week: 6, objective: 'Practica detección y respuesta', topics: ['SIEM', 'Análisis de incidentes', 'Playbooks'] },
      { week: 7, objective: 'Prepara una certificación', topics: ['Dominios de Security+', 'Simulacros', 'Repaso espaciado'] },
      { week: 8, objective: 'Demuestra lo que sabes', topics: ['Proyecto final', 'Portfolio técnico', 'Próximos pasos'] },
    ],
  }),
  generatedAt: new Date().toISOString(),
}
