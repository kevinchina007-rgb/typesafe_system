// 本文件定义 UploadHotelRoomTypeImagePlanner，负责 hotel 模块的上传编排和接口入口。

import type { HotelRoomTypeImageUploadResponse } from '@/microservices/hotel/objects/HotelRoomTypeImageUploadResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

async function toBase64(imageFile: File): Promise<string> {
  const bytes = new Uint8Array(await imageFile.arrayBuffer())
  let binary = ''
  bytes.forEach(byte => {
    binary += String.fromCharCode(byte)
  })
  return window.btoa(binary)
}

export async function uploadHotelRoomTypeImage(imageFile: File): Promise<HotelRoomTypeImageUploadResponse> {
  return executeJsonApiRequest('/UploadHotelRoomTypeImagePlanner', 'POST', {
    originalFileName: imageFile.name,
    mimeType: imageFile.type || 'application/octet-stream',
    fileContentBase64: await toBase64(imageFile),
  })
}
