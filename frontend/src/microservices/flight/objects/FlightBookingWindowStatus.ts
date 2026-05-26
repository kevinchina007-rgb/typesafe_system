export type FlightBookingWindowStatus = 'Available' | 'SurchargeRequired' | 'Expired'
export const flightBookingWindowStatusFromJson = (json: string): FlightBookingWindowStatus =>
  JSON.parse(json) as FlightBookingWindowStatus

export const flightBookingWindowStatusToJson = (value: FlightBookingWindowStatus): string =>
  JSON.stringify(value)
