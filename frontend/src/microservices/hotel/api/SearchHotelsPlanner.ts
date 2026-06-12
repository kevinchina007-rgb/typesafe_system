// 本文件定义 SearchHotelsPlanner，负责 hotel 模块的查询编排和接口入口。

import type { HotelListPlannerResponse } from '@/microservices/hotel/objects/HotelListPlannerResponse'
import type { HotelSearchPlannerRequest } from '@/microservices/hotel/objects/HotelSearchPlannerRequest'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const searchHotelsPlanner = (query: HotelSearchPlannerRequest): Promise<HotelListPlannerResponse> =>
  executeJsonApiRequest('/SearchHotelsPlanner', 'POST', query)
