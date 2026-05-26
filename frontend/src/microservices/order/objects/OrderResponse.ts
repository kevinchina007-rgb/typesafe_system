import type { OrderLineItemResponse } from './OrderLineItemResponse'
import type { PaymentResponse } from './PaymentResponse'
import type { RefundResponse } from './RefundResponse'

export type OrderResponse = {
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
  orderLineItems: OrderLineItemResponse[]
  orderPayments: PaymentResponse[]
  orderRefunds: RefundResponse[]
}
export const orderResponseFromJson = (json: string): OrderResponse =>
  JSON.parse(json) as OrderResponse

export const orderResponseToJson = (value: OrderResponse): string =>
  JSON.stringify(value)
