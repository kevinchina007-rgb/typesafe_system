// 本文件定义 flight 模块的 `FlightDailyLowestPricePlannerResponse`，作为单日价格响应数据并提供 JSON 编解码。

export type FlightDailyLowestPricePlannerResponse = {
  date: string
  lowestPrice: string | null
  currency: string | null
}

export const flightDailyLowestPricePlannerResponseFromJson = (json: string): FlightDailyLowestPricePlannerResponse =>
  JSON.parse(json) as FlightDailyLowestPricePlannerResponse

export const flightDailyLowestPricePlannerResponseToJson = (value: FlightDailyLowestPricePlannerResponse): string =>
  JSON.stringify(value)

