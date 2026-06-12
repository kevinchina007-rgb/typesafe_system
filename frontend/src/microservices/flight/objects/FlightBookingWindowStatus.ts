// 本文件定义 flight 模块的 `FlightBookingWindowStatus`，作为状态枚举并提供 JSON 编解码。

export type FlightBookingWindowStatus = 'Available' | 'SurchargeRequired' | 'Expired'
export const flightBookingWindowStatusFromJson = (json: string): FlightBookingWindowStatus =>
  JSON.parse(json) as FlightBookingWindowStatus

export const flightBookingWindowStatusToJson = (value: FlightBookingWindowStatus): string =>
  JSON.stringify(value)
