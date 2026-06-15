// 本文件定义订单详情响应，和后端 `OrderPlannerResponse` 保持同名镜像。

import type { OrderLineItemPlannerResponse } from './OrderLineItemPlannerResponse'
import type { PaymentPlannerResponse } from './PaymentPlannerResponse'
import type { RefundPlannerResponse } from './RefundPlannerResponse'

export type OrderPlannerResponse = {
  orderId: string
  buyerUserId: string
  orderType: string
  status: string
  orderCurrency: string
  totalPrice: string
  totalCapturedAmount: string
  totalSettledRefundAmount: string
  remainingRefundableAmount: string
  createdAt: string
  paidAt: string | null
  confirmedAt: string | null
  completedAt: string | null
  cancelledAt: string | null
  orderLineItems: OrderLineItemPlannerResponse[]
  orderPayments: PaymentPlannerResponse[]
  orderRefunds: RefundPlannerResponse[]
}

export const orderPlannerResponseFromJson = (json: string): OrderPlannerResponse =>
  JSON.parse(json) as OrderPlannerResponse

export const orderPlannerResponseToJson = (value: OrderPlannerResponse): string =>
  JSON.stringify(value)
