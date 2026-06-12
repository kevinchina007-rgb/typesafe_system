// 本文件定义航班服务前端传输类型，覆盖航班查询、预订和价格相关接口。

export type { FlightBookingWindowStatus } from '@/microservices/flight/objects/FlightBookingWindowStatus'
export type { BookFlightPlannerRequest } from '@/microservices/flight/objects/BookFlightPlannerRequest'
export type { CabinInventoryResponse } from '@/microservices/flight/objects/CabinInventoryResponse'
export type {
  FlightDailyLowestPricesPlannerRequest,
  FlightDailyLowestPricePlannerResponse,
  FlightDailyLowestPricesPlannerResponse,
} from '@/microservices/flight/objects/FlightDailyLowestPrices'
export type { FlightBookingPlannerResponse } from '@/microservices/flight/objects/FlightBookingPlannerResponse'
export type { FlightListPlannerResponse } from '@/microservices/flight/objects/FlightListPlannerResponse'
export type { FlightPlannerResponse } from '@/microservices/flight/objects/FlightPlannerResponse'
export type { FlightSearchPlannerRequest } from '@/microservices/flight/objects/FlightSearchPlannerRequest'
