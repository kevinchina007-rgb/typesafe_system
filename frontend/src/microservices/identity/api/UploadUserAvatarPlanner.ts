// Identity UploadUserAvatarPlanner entry point.
import type { UploadUserAvatarPlannerRequest } from '@/microservices/identity/objects/UploadUserAvatarPlannerRequest'
import type { UserPlannerResponse } from '@/microservices/identity/objects/UserPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const uploadUserAvatarPlanner = async (userId: string, avatarFile: File | string): Promise<UserPlannerResponse> => {
  if (typeof avatarFile === 'string') {
    const payload: UploadUserAvatarPlannerRequest = { userId, publicUrl: avatarFile }
    return executeJsonApiRequest('/UploadUserAvatarPlanner', 'POST', payload)
  }

  const publicUrl = await new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(reader.error ?? new Error('Failed to read avatar'))
    reader.readAsDataURL(avatarFile)
  })
  const payload: UploadUserAvatarPlannerRequest = { userId, publicUrl }
  return executeJsonApiRequest('/UploadUserAvatarPlanner', 'POST', payload)
}
