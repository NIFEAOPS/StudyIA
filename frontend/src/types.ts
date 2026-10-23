export type StudentProfile = {
  name?: string
  email?: string
  learningPath: string
  weeklyAvailabilityHours: number
  durationMonths: number
  professionalGoals: string
}

export type AssessmentOption = {
  id: string
  label?: string
  text: string
}

export type AssessmentQuestion = {
  id: number
  text: string
  options: AssessmentOption[]
}

export type AssessmentAnswer = {
  questionId: number
  selectedOption: string
}

export type RoadmapWeek = {
  week: number
  objective: string
  topics: string[]
  resources?: RoadmapResource[]
}

export type Roadmap = {
  id: string
  studentId: string
  title: string
  level?: 'beginner' | 'intermediate' | 'advanced'
  summary: string
  structuredContent: string
  durationMonths?: number
  generatedAt: string
}

export type RoadmapActivity = {
  id: string
  title: string
  description?: string
  resourceUrl?: string
  position: number
  estimatedMinutes?: number
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'SKIPPED'
  completedAt?: string
}

export type RoadmapResource = {
  id?: string
  title: string
  description?: string
  url: string
  type?: string
  position?: number
}

export type RoadmapStage = {
  id: string
  stageNumber: number
  monthNumber?: number
  weekOfMonth?: number
  title: string
  objective: string
  status: 'LOCKED' | 'AVAILABLE' | 'IN_PROGRESS' | 'COMPLETED'
  progressPercentage: number
  activities: RoadmapActivity[]
  resources: RoadmapResource[]
}

export type Dashboard = {
  roadmap: Roadmap
  metrics: {
    progressPercentage: number
    currentStage: number
    totalStages: number
    completedActivities: number
    totalActivities: number
    currentStreakDays: number
  }

  stages: RoadmapStage[]
}

export type Profile = {
  userId: string
  email: string
  name: string
  university: string | null
  phone: string | null
  role: 'STUDENT' | 'ADMIN'
  emailVerified: boolean
  roadmapCount: number
  roadmapLimit: number
}

export type ParsedRoadmap = {
  level?: 'beginner' | 'intermediate' | 'advanced'
  weeks: RoadmapWeek[]
}
