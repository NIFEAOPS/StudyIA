import { z } from 'zod'

const strongPassword = z.string()
  .min(10, 'Usa al menos 10 caracteres')
  .regex(/[A-Z]/, 'Incluye al menos una mayúscula')
  .regex(/[a-z]/, 'Incluye al menos una minúscula')
  .regex(/[0-9]/, 'Incluye al menos un número')
  .regex(/[^A-Za-z0-9]/, 'Incluye al menos un símbolo')

export const loginSchema = z.object({
  email: z.string().trim().email('Escribe un correo válido'),
  password: z.string().min(1, 'Escribe tu contraseña'),
})

export const registerSchema = z.object({
  name: z.string().trim().min(2, 'Escribe tu nombre').max(120, 'El nombre es demasiado largo'),
  email: z.string().trim().email('Escribe un correo válido'),
  password: strongPassword,
  confirmPassword: z.string().min(1, 'Confirma tu contraseña'),
}).refine((data) => data.password === data.confirmPassword, {
  message: 'Las contraseñas no coinciden',
  path: ['confirmPassword'],
})

export type LoginFormValues = z.infer<typeof loginSchema>
export type RegisterFormValues = z.infer<typeof registerSchema>
