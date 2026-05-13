import type { UserResponse } from '@/microservices/auth/objects/UserResponse'
import { createSingleFileFormData, executeApiRequest, executeJsonApiRequest, executeMultipartApiRequest } from '@/microservices/common/api/ApiTransport'

export const createUser = (payload: { email: string; nickname: string; phone: string }): Promise<UserResponse> =>
    executeJsonApiRequest('/users', 'POST', payload)

export const loginUser = (payload: { email: string }): Promise<UserResponse> =>
    executeJsonApiRequest('/session/login', 'POST', payload)

export const getUser = (userId: string): Promise<UserResponse> =>
    executeApiRequest(`/users/${userId}`)

export const uploadUserAvatar = (userId: string, avatarFile: File): Promise<UserResponse> =>
    executeMultipartApiRequest(`/users/${userId}/avatar`, 'POST', createSingleFileFormData('avatar', avatarFile))
