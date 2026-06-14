// 本文件定义 train 模块的 `TrainPlannerResponse`，用于表示火车详情并提供 JSON 编解码。

import type { TrainStopPlannerResponse } from './TrainStopPlannerResponse'
import type { TrainSeatInventoryPlannerResponse } from './TrainSeatInventoryPlannerResponse'
import type { TrainSeatPlannerResponse } from './TrainSeatPlannerResponse'
import type { TrainSegmentPricePlannerResponse } from './TrainSegmentPricePlannerResponse'
import type { TrainRefundPolicyPlannerResponse } from './TrainRefundPolicyPlannerResponse'

export type TrainPlannerResponse = {
  trainId: string
  trainNumber: string
  saleStartsAt: string
  status: string
  stops: TrainStopPlannerResponse[]
  seatInventories: TrainSeatInventoryPlannerResponse[]
  seats: TrainSeatPlannerResponse[]
  segmentPrices: TrainSegmentPricePlannerResponse[]
  refundPolicies: TrainRefundPolicyPlannerResponse[]
}
export const trainPlannerResponseFromJson = (json: string): TrainPlannerResponse =>
  JSON.parse(json) as TrainPlannerResponse

export const trainPlannerResponseToJson = (value: TrainPlannerResponse): string =>
  JSON.stringify(value)
