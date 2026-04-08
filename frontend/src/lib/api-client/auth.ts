import type {
  AuthSessionListResponse,
  CurrentManagerSessionResponse,
  CurrentUserSessionResponse,
  HealthResponse,
  ManagerType,
} from '../api-dtos'
import { executeApiRequest, executeJsonApiRequest } from '../api-transport'

export const authApiClient = {
  getHealth: (): Promise<HealthResponse> => executeApiRequest('/health'),

  signupUser: (payload: {
    email: string
    nickname: string
    phone: string
    password: string
  }): Promise<CurrentUserSessionResponse> =>
    executeJsonApiRequest('/auth/signup', 'POST', payload),

  loginUserWithPassword: (payload: {
    email: string
    password: string
  }): Promise<CurrentUserSessionResponse> =>
    executeJsonApiRequest('/auth/login', 'POST', payload),

  logoutUser: (): Promise<{ status: string }> =>
    executeJsonApiRequest('/auth/logout', 'POST'),

  getCurrentUserSession: (): Promise<CurrentUserSessionResponse> =>
    executeApiRequest('/auth/me'),

  changeUserPassword: (payload: { currentPassword: string; newPassword: string }): Promise<{ status: string }> =>
    executeJsonApiRequest('/auth/change-password', 'POST', payload),

  listUserSessions: (): Promise<AuthSessionListResponse> =>
    executeApiRequest('/auth/sessions'),

  logoutCurrentUserSession: (): Promise<{ status: string }> =>
    executeJsonApiRequest('/auth/logout-current', 'POST'),

  logoutOtherUserSessions: (): Promise<{ revokedCount: number }> =>
    executeJsonApiRequest('/auth/logout-others', 'POST'),

  loginManagerAuth: (payload: {
    managerType: ManagerType
    email: string
    password: string
  }): Promise<CurrentManagerSessionResponse> =>
    executeJsonApiRequest('/manager-auth/login', 'POST', payload),

  logoutManagerAuth: (): Promise<{ status: string }> =>
    executeJsonApiRequest('/manager-auth/logout', 'POST'),

  getCurrentManagerSession: (): Promise<CurrentManagerSessionResponse> =>
    executeApiRequest('/manager-auth/me'),

  changeManagerPassword: (payload: { currentPassword: string; newPassword: string }): Promise<{ status: string }> =>
    executeJsonApiRequest('/manager-auth/change-password', 'POST', payload),

  listManagerSessions: (): Promise<AuthSessionListResponse> =>
    executeApiRequest('/manager-auth/sessions'),

  logoutCurrentManagerSession: (): Promise<{ status: string }> =>
    executeJsonApiRequest('/manager-auth/logout-current', 'POST'),

  logoutOtherManagerSessions: (): Promise<{ revokedCount: number }> =>
    executeJsonApiRequest('/manager-auth/logout-others', 'POST'),
}
