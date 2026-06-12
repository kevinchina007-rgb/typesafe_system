// 本文件定义 train 模块的 `TrainResponse`，作为响应数据并提供 JSON 编解码。

import type { TrainStopResponse } from './TrainStopResponse'
import type { TrainSeatInventoryResponse } from './TrainSeatInventoryResponse'
import type { TrainSeatResponse } from './TrainSeatResponse'
import type { TrainSegmentPriceResponse } from './TrainSegmentPriceResponse'
import type { TrainRefundPolicyResponse } from './TrainRefundPolicyResponse'

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
export const trainResponseFromJson = (json: string): TrainResponse =>
  JSON.parse(json) as TrainResponse

export const trainResponseToJson = (value: TrainResponse): string =>
  JSON.stringify(value)
