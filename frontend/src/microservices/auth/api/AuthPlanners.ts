import type { AuthSessionListResponse } from '@/microservices/auth/objects/AuthSessionListResponse'

import type { CurrentUserSessionResponse } from '@/microservices/auth/objects/CurrentUserSessionResponse'

import type { HealthResponse } from '@/microservices/common/objects/HealthResponse'
import { executeApiRequest, executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const getHealth = (): Promise<HealthResponse> => executeApiRequest('/health')

export const signupUser = (payload: {
    email: string
    nickname: string
    phone: string
    password: string
  }): Promise<CurrentUserSessionResponse> =>
    executeJsonApiRequest('/auth/signup', 'POST', payload)

export const loginUserWithPassword = (payload: {
    email: string
    password: string
  }): Promise<CurrentUserSessionResponse> =>
    executeJsonApiRequest('/auth/login', 'POST', payload)

export const logoutUser = (): Promise<{ status: string }> =>
    executeJsonApiRequest('/auth/logout', 'POST')

export const getCurrentUserSession = (): Promise<CurrentUserSessionResponse> =>
    executeApiRequest('/auth/me')

export const changeUserPassword = (payload: { currentPassword: string; newPassword: string }): Promise<{ status: string }> =>
    executeJsonApiRequest('/auth/change-password', 'POST', payload)

export const listUserSessions = (): Promise<AuthSessionListResponse> =>
    executeApiRequest('/auth/sessions')

export const logoutCurrentUserSession = (): Promise<{ status: string }> =>
    executeJsonApiRequest('/auth/logout-current', 'POST')

export const logoutOtherUserSessions = (): Promise<{ revokedCount: number }> =>
    executeJsonApiRequest('/auth/logout-others', 'POST')
