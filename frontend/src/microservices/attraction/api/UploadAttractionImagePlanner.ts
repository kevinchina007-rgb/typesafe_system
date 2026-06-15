// 本文件定义 UploadAttractionImagePlanner，负责 attraction 模块的上传编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { UploadAttractionImagePlannerResponse } from '@/microservices/attraction/objects/UploadAttractionImagePlannerResponse'

async function toBase64(imageFile: File): Promise<string> {
  const bytes = new Uint8Array(await imageFile.arrayBuffer())
  let binary = ''
  bytes.forEach(byte => {
    binary += String.fromCharCode(byte)
  })
  return window.btoa(binary)
}

export async function uploadAttractionImage(imageFile: File): Promise<UploadAttractionImagePlannerResponse> {
  return executeJsonApiRequest('/UploadAttractionImagePlanner', 'POST', {
    originalFileName: imageFile.name,
    mimeType: imageFile.type || 'application/octet-stream',
    fileContentBase64: await toBase64(imageFile),
  })
}
