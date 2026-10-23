import { apiClient } from './apiClient'
import type { AssessmentAnswer, AssessmentQuestion, Dashboard, Profile, Roadmap, StudentProfile } from './types'

type GenerateAssessmentResponse = {
  assessmentId: string
  questions: AssessmentQuestion[]
}

export async function generateAssessment(profile: StudentProfile): Promise<GenerateAssessmentResponse> {
  const payload = {
    learningPath: profile.learningPath,
    weeklyAvailabilityHours: profile.weeklyAvailabilityHours,
    durationMonths: profile.durationMonths,
    professionalGoals: profile.professionalGoals,
  }

  const { data } = await apiClient.post<GenerateAssessmentResponse>('/assessments/generate', payload)
  return data
}

export async function evaluateAssessment(assessmentId: string, answers: AssessmentAnswer[]): Promise<Roadmap> {
  const payload = {
    assessmentId,
    answers: answers.map(({ questionId, selectedOption }) => ({ questionId, selectedOption })),
  }

  const { data } = await apiClient.post<Roadmap>('/assessments/evaluate', payload)
  return data
}

export async function generateRoadmap(profile: StudentProfile): Promise<Roadmap> {
  const { data } = await apiClient.post<Roadmap>('/roadmaps', profile)
  return data
}

export async function getDashboard(): Promise<Dashboard> {
  const { data } = await apiClient.get<Dashboard>('/dashboard')
  return data
}

export async function completeActivity(activityId: string): Promise<Dashboard> {
  const { data } = await apiClient.patch<Dashboard>(`/activities/${activityId}/complete`)
  return data
}

export async function getProfile(): Promise<Profile> {
  const { data } = await apiClient.get<Profile>('/profile')
  return data
}

export async function updateProfile(university: string, phone: string): Promise<Profile> {
  const { data } = await apiClient.patch<Profile>('/profile', { university, phone })
  return data
}

export async function changePassword(currentPassword: string, newPassword: string): Promise<Profile> {
  const { data } = await apiClient.patch<Profile>('/profile/password', { currentPassword, newPassword })
  return data
}
