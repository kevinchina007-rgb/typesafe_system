// 本文件定义 UploadUserAvatarPlanner，负责 identity 模块的上传编排和接口入口。

import type { UserResponse } from '@/microservices/auth/objects/UserResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const uploadUserAvatar = async (userId: string, avatarFile: File | string): Promise<UserResponse> => {
  if (typeof avatarFile === 'string') {
    return executeJsonApiRequest('/UploadUserAvatarPlanner', 'POST', { userId, publicUrl: avatarFile })
  }

  const publicUrl = await new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(reader.error ?? new Error('Failed to read avatar'))
    reader.readAsDataURL(avatarFile)
  })
  return executeJsonApiRequest('/UploadUserAvatarPlanner', 'POST', { userId, publicUrl })
}
