// UploadReviewImagePlanner：content 域评论图片上传入口。

import type { UploadReviewImagePlannerRequest } from '@/microservices/content/objects/UploadReviewImagePlannerRequest'
import type { ContentImagePlannerResponse } from '@/microservices/content/objects/ContentImagePlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const uploadReviewImage = async (userId: string, imageFile: File): Promise<ContentImagePlannerResponse> => {
  const bytes = new Uint8Array(await imageFile.arrayBuffer())
  let binary = ''
  bytes.forEach(byte => {
    binary += String.fromCharCode(byte)
  })
  const payload: UploadReviewImagePlannerRequest = {
    userId,
    originalFileName: imageFile.name,
    contentType: imageFile.type || 'application/octet-stream',
    base64Content: window.btoa(binary),
  }
  return executeJsonApiRequest('/UploadReviewImagePlanner', 'POST', payload)
}
