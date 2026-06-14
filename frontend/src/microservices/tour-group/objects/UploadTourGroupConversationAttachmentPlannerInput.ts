// This file defines `tour-group` module `UploadTourGroupConversationAttachmentPlannerInput` as request payload data and provides JSON helpers.

import type { UploadConversationAttachmentPlannerRequest } from './UploadConversationAttachmentPlannerRequest'

export type UploadTourGroupConversationAttachmentPlannerInput = {
  conversationId: string
  sessionId: string
  payload: UploadConversationAttachmentPlannerRequest
}
export const uploadTourGroupConversationAttachmentPlannerInputFromJson = (json: string): UploadTourGroupConversationAttachmentPlannerInput =>
  JSON.parse(json) as UploadTourGroupConversationAttachmentPlannerInput

export const uploadTourGroupConversationAttachmentPlannerInputToJson = (value: UploadTourGroupConversationAttachmentPlannerInput): string =>
  JSON.stringify(value)
