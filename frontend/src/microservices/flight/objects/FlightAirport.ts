// 本文件定义 flight 模块的 `FlightAirport`，作为传输数据并提供 JSON 编解码。

export type FlightAirport = {
  airportCode: string
  cityName: string
  airportName: string
}

export const flightAirportFromJson = (json: string): FlightAirport =>
  JSON.parse(json) as FlightAirport

export const flightAirportToJson = (value: FlightAirport): string =>
  JSON.stringify(value)
