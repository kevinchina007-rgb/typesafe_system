import type { FeedbackSenderRole } from './FeedbackSenderRole'

export type SendFeedbackMessageRequest = {
  senderRole: FeedbackSenderRole
  senderDisplayName: string
  body: string
}
export const sendFeedbackMessageRequestFromJson = (json: string): SendFeedbackMessageRequest =>
  JSON.parse(json) as SendFeedbackMessageRequest

export const sendFeedbackMessageRequestToJson = (value: SendFeedbackMessageRequest): string =>
  JSON.stringify(value)
