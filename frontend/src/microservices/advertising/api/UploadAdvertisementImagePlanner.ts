// 本文件定义 advertising 模块的 `UploadAdvertisementImagePlanner`，负责广告图片上传入口。

import type { UploadAdvertisementImageRequest } from '@/microservices/advertising/objects/UploadAdvertisementImageRequest'
import type { UploadAdvertisementImageResponse } from '@/microservices/advertising/objects/UploadAdvertisementImageResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

async function toBase64(imageFile: File): Promise<string> {
  const bytes = new Uint8Array(await imageFile.arrayBuffer())
  let binary = ''
  bytes.forEach(byte => {
    binary += String.fromCharCode(byte)
  })
  return window.btoa(binary)
}

export async function uploadAdvertisementImage(imageFile: File): Promise<UploadAdvertisementImageResponse> {
  const payload: UploadAdvertisementImageRequest = {
    originalFileName: imageFile.name,
    mimeType: imageFile.type || 'application/octet-stream',
    fileContentBase64: await toBase64(imageFile),
  }

  return executeJsonApiRequest('/UploadAdvertisementImagePlanner', 'POST', payload)
}
