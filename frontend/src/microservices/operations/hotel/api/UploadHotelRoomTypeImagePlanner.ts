// 本文件定义 UploadHotelRoomTypeImagePlanner，负责 operations 模块的上传编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { HotelRoomTypeImageUploadResponse } from '@/microservices/hotel/objects/HotelRoomTypeImageUploadResponse'

export async function uploadHotelRoomTypeImage(imageFile: File): Promise<HotelRoomTypeImageUploadResponse> {
  const bytes = new Uint8Array(await imageFile.arrayBuffer())
  let binary = ''
  bytes.forEach(byte => {
    binary += String.fromCharCode(byte)
  })
  return executeJsonApiRequest('/UploadHotelRoomTypeImagePlanner', 'POST', {
    originalFileName: imageFile.name,
    mimeType: imageFile.type || 'application/octet-stream',
    fileContentBase64: window.btoa(binary),
  })
}
