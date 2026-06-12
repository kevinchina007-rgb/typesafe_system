// 本文件定义 GetHotelDetailsPlanner，负责 hotel 模块的获取编排和接口入口。

import type { HotelPlannerResponse } from '@/microservices/hotel/objects/HotelPlannerResponse'
import type { HotelSearchPlannerRequest } from '@/microservices/hotel/objects/HotelSearchPlannerRequest'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const getHotelDetailsPlanner = (
  hotelId: string,
  query?: Pick<HotelSearchPlannerRequest, 'checkInDate' | 'checkOutDate'>,
): Promise<HotelPlannerResponse> =>
  executeJsonApiRequest('/GetHotelDetailsPlanner', 'POST', {
    hotelId,
    checkInDate: query?.checkInDate,
    checkOutDate: query?.checkOutDate,
  })
