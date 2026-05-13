import type { AuthSessionListResponse } from '@/microservices/auth/objects/AuthSessionListResponse'
import type { CurrentManagerSessionResponse } from '@/microservices/auth/objects/CurrentManagerSessionResponse'

import type { ManagerType } from '@/microservices/auth/objects/ManagerType'

import { executeApiRequest, executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const loginManagerAuth = (payload: {
    managerType: ManagerType
    email: string
    password: string
  }): Promise<CurrentManagerSessionResponse> =>
    executeJsonApiRequest('/manager-auth/login', 'POST', payload)

export const logoutManagerAuth = (): Promise<{ status: string }> =>
    executeJsonApiRequest('/manager-auth/logout', 'POST')

export const getCurrentManagerSession = (): Promise<CurrentManagerSessionResponse> =>
    executeApiRequest('/manager-auth/me')

export const changeManagerPassword = (payload: { currentPassword: string; newPassword: string }): Promise<{ status: string }> =>
    executeJsonApiRequest('/manager-auth/change-password', 'POST', payload)

export const listManagerSessions = (): Promise<AuthSessionListResponse> =>
    executeApiRequest('/manager-auth/sessions')

export const logoutCurrentManagerSession = (): Promise<{ status: string }> =>
    executeJsonApiRequest('/manager-auth/logout-current', 'POST')

export const logoutOtherManagerSessions = (): Promise<{ revokedCount: number }> =>
    executeJsonApiRequest('/manager-auth/logout-others', 'POST')
