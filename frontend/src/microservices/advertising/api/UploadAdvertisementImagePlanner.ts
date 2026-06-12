// 本文件定义 UploadAdvertisementImagePlanner，负责 advertising 模块的上传编排和接口入口。

import type { AdvertisementImageUploadResponse } from '@/microservices/advertising/objects/AdvertisementImageUploadResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

async function toBase64(imageFile: File): Promise<string> {
  const bytes = new Uint8Array(await imageFile.arrayBuffer())
  let binary = ''
  bytes.forEach(byte => {
    binary += String.fromCharCode(byte)
  })
  return window.btoa(binary)
}

export async function uploadAdvertisementImage(imageFile: File): Promise<AdvertisementImageUploadResponse> {
  return executeJsonApiRequest('/UploadAdvertisementImagePlanner', 'POST', {
    originalFileName: imageFile.name,
    mimeType: imageFile.type || 'application/octet-stream',
    fileContentBase64: await toBase64(imageFile),
  })
}
