// 本文件定义 order 模块的 `OrderLineItemResponse`，作为响应数据并提供 JSON 编解码。

import type { FlightItemDetailsResponse } from './FlightItemDetailsResponse'
import type { HotelItemDetailsResponse } from './HotelItemDetailsResponse'
import type { TrainItemDetailsResponse } from './TrainItemDetailsResponse'
import type { AttractionItemDetailsResponse } from './AttractionItemDetailsResponse'
import type { SupplierReviewDecisionResponse } from './SupplierReviewDecisionResponse'

export type OrderLineItemResponse = {
  orderItemId: string
  orderItemKind: string
  orderItemStatus: string
  supplierReviewStatus: string
  supplierReviewDecision: SupplierReviewDecisionResponse | null
  bookedAmount: string
  bookedCurrency: string
  summaryLabel: string
  flightDetails: FlightItemDetailsResponse | null
  hotelDetails: HotelItemDetailsResponse | null
  trainDetails: TrainItemDetailsResponse | null
  attractionDetails: AttractionItemDetailsResponse | null
}
export const orderLineItemResponseFromJson = (json: string): OrderLineItemResponse =>
  JSON.parse(json) as OrderLineItemResponse

export const orderLineItemResponseToJson = (value: OrderLineItemResponse): string =>
  JSON.stringify(value)
