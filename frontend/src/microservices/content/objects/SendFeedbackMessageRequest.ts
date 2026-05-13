import type { FeedbackSenderRole } from './FeedbackSenderRole'

export type SendFeedbackMessageRequest = {
  senderRole: FeedbackSenderRole
  senderDisplayName: string
  body: string
}
