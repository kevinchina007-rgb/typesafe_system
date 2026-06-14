// 本文件定义 flight 模块的 `FlightDailyLowestPricesPlannerResponse`，作为价格列表响应数据并提供 JSON 编解码。

import type { FlightDailyLowestPricePlannerResponse } from './FlightDailyLowestPricePlannerResponse'

export type FlightDailyLowestPricesPlannerResponse = {
  prices: FlightDailyLowestPricePlannerResponse[]
}

export const flightDailyLowestPricesPlannerResponseFromJson = (json: string): FlightDailyLowestPricesPlannerResponse =>
  JSON.parse(json) as FlightDailyLowestPricesPlannerResponse

export const flightDailyLowestPricesPlannerResponseToJson = (value: FlightDailyLowestPricesPlannerResponse): string =>
  JSON.stringify(value)

