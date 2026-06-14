// 本文件定义 train 模块的 `CreateTrainJourneyPlannerRequest`，用于创建火车行程并提供 JSON 编解码。

export type TrainStopPlannerRequest = {
  stationCode: string
  stationName: string
  arrivalTime?: string | null
  departureTime?: string | null
}

export type TrainSeatInventoryPlannerRequest = {
  seatClass: string
  totalSeats: number
  saleableSeats: number
  carriageCount: number
  rowsPerCarriage: number
  seatLayoutSpec: string
}

export type TrainSegmentPricePlannerRequest = {
  fromStationCode: string
  toStationCode: string
  seatClass: string
  amount: string
  currency: string
}

export type TrainRefundPolicyPlannerRequest = {
  startOffsetMinutesBeforeDeparture: number
  endOffsetMinutesBeforeDeparture: number
  refundType: string
  refundRate: string
}

export type CreateTrainJourneyPlannerRequest = {
  managerId: string
  trainNumber: string
  saleStartsAt: string
  stops: TrainStopPlannerRequest[]
  seatInventories: TrainSeatInventoryPlannerRequest[]
  segmentPrices: TrainSegmentPricePlannerRequest[]
  refundPolicies: TrainRefundPolicyPlannerRequest[]
}
export const createTrainJourneyPlannerRequestFromJson = (json: string): CreateTrainJourneyPlannerRequest =>
  JSON.parse(json) as CreateTrainJourneyPlannerRequest

export const createTrainJourneyPlannerRequestToJson = (value: CreateTrainJourneyPlannerRequest): string =>
  JSON.stringify(value)
