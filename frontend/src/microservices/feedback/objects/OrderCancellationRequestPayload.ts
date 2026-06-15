// 本文件定义 content 模块的 `OrderCancellationRequestPayload`，作为消息载荷并提供 JSON 编解码。

import type { FeedbackSenderRole } from './FeedbackSenderRole'

export type OrderCancellationRequestStatus = 'pending' | 'approved' | 'rejected' | 'needMoreInfo'

export type OrderCancellationRequestPayload = {
  orderId: string
  orderTitle: string | null
  reason: string
  requestedRefundAmount: number | null
  status: OrderCancellationRequestStatus
  createdAt: string
  handledAt: string | null
  handledBy: string | null
  handlerRole: FeedbackSenderRole | null
  managerNote: string | null
}

export const orderCancellationRequestPayloadFromJson = (json: string): OrderCancellationRequestPayload =>
  JSON.parse(json) as OrderCancellationRequestPayload

export const orderCancellationRequestPayloadToJson = (value: OrderCancellationRequestPayload): string =>
  JSON.stringify(value)
