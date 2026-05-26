import type { FeedbackSenderRole } from './FeedbackSenderRole'
import type { FeedbackMessageType } from './FeedbackMessageType'
import type { OrderCancellationRequestPayload } from './OrderCancellationRequestPayload'

export type FeedbackMessageResponse = {
  messageId: string
  threadId: string
  senderId: string
  senderRole: FeedbackSenderRole
  senderDisplayName: string
  messageType: FeedbackMessageType
  content: string
  payload: OrderCancellationRequestPayload | null
  isRead: boolean
  createdAt: string
}

export const feedbackMessageResponseFromJson = (json: string): FeedbackMessageResponse =>
  JSON.parse(json) as FeedbackMessageResponse

export const feedbackMessageResponseToJson = (value: FeedbackMessageResponse): string =>
  JSON.stringify(value)
