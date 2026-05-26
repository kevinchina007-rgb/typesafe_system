import type { TourGroupMessageAttachmentResponse } from './TourGroupMessageAttachmentResponse'
import type { TourGroupMessageReactionResponse } from './TourGroupMessageReactionResponse'

export type TourGroupMessageResponse = {
  messageId: string
  conversationId: string
  messageType: string
  senderUserId: string
  senderDisplayName: string
  senderAvatarUrl: string | null
  content: string
  replyToMessageId: string | null
  replyToPreview: string | null
  status: string
  createdAt: string
  updatedAt: string
  attachments: TourGroupMessageAttachmentResponse[]
  reactions: TourGroupMessageReactionResponse[]
  canEdit: boolean
  canDelete: boolean
  canRecall: boolean
  canReact: boolean
  isMine: boolean
}
export const tourGroupMessageResponseFromJson = (json: string): TourGroupMessageResponse =>
  JSON.parse(json) as TourGroupMessageResponse

export const tourGroupMessageResponseToJson = (value: TourGroupMessageResponse): string =>
  JSON.stringify(value)
