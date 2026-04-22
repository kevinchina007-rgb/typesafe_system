import type { AttractionResponse, TrainResponse } from './resources'

export type UserResponse = {
  userId: string
  email: string
  nickname: string
  phone: string
  avatarUrl: string | null
  status: string
  membershipLevel: string
  points: number
  defaultTravelerProfileId: string | null
  createdAt: string
}

export type CurrentUserSessionResponse = {
  user: UserResponse
  expiresAt: string
}

export type AuthSessionResponse = {
  sessionId: string
  createdAt: string
  lastSeenAt: string
  expiresAt: string
  status: string
  isCurrent: boolean
}

export type AuthSessionListResponse = {
  sessions: AuthSessionResponse[]
}

export type ManagerType = 'airline' | 'hotel' | 'train' | 'attraction' | 'siteAdmin'

export type ManagerSessionResponse = {
  managerId: string
  managerType: string
  email: string
  displayName: string
  status: string
  scopeId: string
  createdAt: string
}

export type CurrentManagerSessionResponse = {
  managerId: string
  managerType: string
  email: string
  displayName: string
  status: string
  scopeId: string
  createdAt: string
  expiresAt: string
}

export type TrainAdminSessionResponse = {
  managerId: string
  operatorCode: string
  email: string
  displayName: string
  status: string
  managedTrains: TrainResponse[]
}

export type AttractionAdminSessionResponse = {
  managerId: string
  email: string
  displayName: string
  status: string
  managedAttractions: AttractionResponse[]
}
