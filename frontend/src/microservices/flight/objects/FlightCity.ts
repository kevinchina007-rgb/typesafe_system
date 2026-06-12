// 本文件定义 flight 模块的 `FlightCity`，作为传输数据并提供 JSON 编解码。

import type { FlightAirport } from './FlightAirport'

export type FlightCity = {
  cityName: string
  airports: FlightAirport[]
}

export const flightCityFromJson = (json: string): FlightCity =>
  JSON.parse(json) as FlightCity

export const flightCityToJson = (value: FlightCity): string =>
  JSON.stringify(value)
