// 本文件定义 FlightDailyLowestPricesPlanner，负责 flight 模块的处理编排和接口入口。

import type {
  FlightDailyLowestPricesPlannerRequest,
} from '@/microservices/flight/objects/FlightDailyLowestPricesPlannerRequest'
import type { FlightDailyLowestPricesPlannerResponse } from '@/microservices/flight/objects/FlightDailyLowestPricesPlannerResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const flightDailyLowestPricesPlanner = (
  payload: FlightDailyLowestPricesPlannerRequest,
): Promise<FlightDailyLowestPricesPlannerResponse> =>
  executeJsonApiRequest('/FlightDailyLowestPricesPlanner', 'POST', payload)
