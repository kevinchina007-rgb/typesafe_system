import type { FeedbackSenderRole } from './FeedbackSenderRole'

export type FeedbackMessageResponse = {
  messageId: string
  senderRole: FeedbackSenderRole
  senderDisplayName: string
  body: string
  sentAt: string
}
