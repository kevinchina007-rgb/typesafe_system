export type TrainStopResponse = {
  stopId: string
  stationCode: string
  stationName: string
  sequenceNo: number
  arrivalTime: string | null
  departureTime: string | null
}

export type TrainSeatInventoryResponse = {
  inventoryId: string
  seatClass: string
  totalSeats: number
  saleableSeats: number
  status: string
}

export type TrainSeatResponse = {
  seatId: string
  carriageNo: number
  seatNo: string
  seatLabel: string
  seatClass: string
  seatPositionType: string
  status: string
}

export type TrainSegmentPriceResponse = {
  fromStationCode: string
  toStationCode: string
  seatClass: string
  amount: string
  currency: string
}

export type TrainRefundPolicyResponse = {
  startOffsetMinutesBeforeDeparture: number
  endOffsetMinutesBeforeDeparture: number
  refundType: string
  refundRate: string
}

export type TrainResponse = {
  trainId: string
  trainNumber: string
  saleStartsAt: string
  status: string
  stops: TrainStopResponse[]
  seatInventories: TrainSeatInventoryResponse[]
  seats: TrainSeatResponse[]
  segmentPrices: TrainSegmentPriceResponse[]
  refundPolicies: TrainRefundPolicyResponse[]
}

export type TrainSeatAssignmentResponse = {
  travelerId: string
  seatId: string
  carriageNo: number
  seatNo: string
  seatLabel: string
  seatPositionType: string
}

export type TrainListResponse = {
  trains: TrainResponse[]
}

export type TrainSearchQueryDto = {
  fromStation?: string
  toStation?: string
  date?: string
}

export type BookTrainItemRequestDto = {
  trainId: string
  travelerIds: string[]
  fromStationCode: string
  toStationCode: string
  seatClass: string
  seatPreference?: string | null
}
