// 本文件定义 content 模块的 `HandleOrderCancellationRequest`，作为请求参数并提供 JSON 编解码。

import type { FeedbackSenderRole } from './FeedbackSenderRole'
import type { OrderCancellationRequestStatus } from './OrderCancellationRequestPayload'

export type HandleOrderCancellationRequest = {
  threadId: string
  messageId: string
  status: Exclude<OrderCancellationRequestStatus, 'pending'>
  managerNote?: string | null
  handledBy?: string | null
  handlerRole?: FeedbackSenderRole | null
}

export const handleOrderCancellationRequestFromJson = (json: string): HandleOrderCancellationRequest =>
  JSON.parse(json) as HandleOrderCancellationRequest

export const handleOrderCancellationRequestToJson = (value: HandleOrderCancellationRequest): string =>
  JSON.stringify(value)
