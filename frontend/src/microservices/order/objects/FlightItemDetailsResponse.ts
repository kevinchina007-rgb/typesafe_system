// 本文件定义 order 模块的 `FlightItemDetailsResponse`，作为详情响应数据并提供 JSON 编解码。

export type FlightItemDetailsResponse = {
  airlineName: string
  airlineCode: string
  flightId: string
  flightNumber: string
  departureAirport: string
  arrivalAirport: string
  departureTime: string
  arrivalTime: string
  cabinClass: string
  travelerIds: string[]
  reservationStatus: string | null
  reservationExpiresAt: string | null
  unitPrice: string
  currency: string
}
export const flightItemDetailsResponseFromJson = (json: string): FlightItemDetailsResponse =>
  JSON.parse(json) as FlightItemDetailsResponse

export const flightItemDetailsResponseToJson = (value: FlightItemDetailsResponse): string =>
  JSON.stringify(value)
