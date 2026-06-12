// 本文件定义 content 模块的 `FeedbackMessageResponse`，作为响应数据并提供 JSON 编解码。

import type { FeedbackSenderRole } from './FeedbackSenderRole'
import type { FeedbackMessageType } from './FeedbackMessageType'
import type { OrderCancellationRequestPayload } from './OrderCancellationRequestPayload'
import type { ComplaintCardPayload } from './ComplaintCardPayload'

export type FeedbackMessageResponse = {
  messageId: string
  threadId: string
  senderId: string
  senderRole: FeedbackSenderRole
  senderDisplayName: string
  messageType: FeedbackMessageType
  content: string
  payload: OrderCancellationRequestPayload | null
  complaintPayload?: ComplaintCardPayload | null
  isRead: boolean
  createdAt: string
}

export const feedbackMessageResponseFromJson = (json: string): FeedbackMessageResponse =>
  JSON.parse(json) as FeedbackMessageResponse

export const feedbackMessageResponseToJson = (value: FeedbackMessageResponse): string =>
  JSON.stringify(value)
