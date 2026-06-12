// 本文件定义 CreateManagerRoomTypePlanner，负责 operations 模块的创建编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { HotelPlannerResponse } from '@/microservices/hotel/objects/HotelPlannerResponse'

export const createManagerRoomType = (payload: {
  managerId: string
  roomTypeName: string
  capacity: number
  bedType: string
  nightlyPrice: string
  currency: string
  availableRooms: number
  inventoryStartDate: string
  inventoryEndDate: string
  roomImageUrl?: string | null
}): Promise<HotelPlannerResponse> =>
  executeJsonApiRequest('/CreateManagerRoomTypePlanner', 'POST', payload)
