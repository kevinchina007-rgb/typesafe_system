// 本文件定义 GetHotelDetailsPlanner，负责 hotel 模块的获取编排和接口入口。

import type { GetHotelDetailsPlannerRequest } from '@/microservices/hotel/objects/GetHotelDetailsPlannerRequest'
import type { HotelPlannerResponse } from '@/microservices/hotel/objects/HotelPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const getHotelDetailsPlanner = (
  hotelId: string,
  query?: Pick<GetHotelDetailsPlannerRequest, 'checkInDate' | 'checkOutDate'>,
): Promise<HotelPlannerResponse> => {
  const payload: GetHotelDetailsPlannerRequest = {
    hotelId,
    checkInDate: query?.checkInDate,
    checkOutDate: query?.checkOutDate,
  }

  return executeJsonApiRequest('/GetHotelDetailsPlanner', 'POST', payload)
}
