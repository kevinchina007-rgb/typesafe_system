// 本文件定义 SearchFlightsPlanner，负责 flight 模块的查询编排和接口入口。

import type { FlightListPlannerResponse } from '@/microservices/flight/objects/FlightListPlannerResponse'
import type { FlightSearchPlannerRequest } from '@/microservices/flight/objects/FlightSearchPlannerRequest'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const searchFlightsPlanner = (query: FlightSearchPlannerRequest): Promise<FlightListPlannerResponse> =>
  executeJsonApiRequest('/SearchFlightsPlanner', 'POST', query)
