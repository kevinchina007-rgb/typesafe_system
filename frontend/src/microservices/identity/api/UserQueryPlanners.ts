import type { UserResponse } from '@/microservices/auth/objects/UserResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const createUser = (payload: { email: string; nickname: string; phone: string }): Promise<UserResponse> =>
  executeJsonApiRequest('/CreateUserPlanner', 'POST', payload)

export const loginUser = (payload: { email: string }): Promise<UserResponse> =>
  executeJsonApiRequest('/LoginUserPlanner', 'POST', payload)

export const getUser = (userId: string): Promise<UserResponse> =>
  executeJsonApiRequest('/GetUserPlanner', 'POST', { userId })

export const uploadUserAvatar = async (userId: string, avatarFile: File): Promise<UserResponse> => {
  const publicUrl = await new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(reader.error ?? new Error('头像读取失败'))
    reader.readAsDataURL(avatarFile)
  })
  return executeJsonApiRequest('/UploadUserAvatarPlanner', 'POST', { userId, publicUrl })
}
