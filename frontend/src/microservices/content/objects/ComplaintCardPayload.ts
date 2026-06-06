import type { FeedbackMessageType } from './FeedbackMessageType'
import type { FeedbackSenderRole } from './FeedbackSenderRole'

export type ComplaintMessageSnapshot = {
  messageId: string
  senderRole: FeedbackSenderRole
  senderDisplayName: string
  content: string
  messageType: FeedbackMessageType
  createdAt: string
}

export type ComplaintCardPayload = {
  complaintId: string
  sourceThreadId: string
  managerThreadId: string | null
  userExplanation: string
  summary: string
  targetDisplayName: string
  selectedMessages: ComplaintMessageSnapshot[]
  createdAt: string
}
