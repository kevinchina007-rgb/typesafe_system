// This file defines `tour-group` module `UploadConversationAttachmentPlannerRequest` as request payload data and provides JSON helpers.

export type UploadConversationAttachmentPlannerRequest = {
  fileName: string
  mimeType: string
  base64Content: string
}
export const uploadConversationAttachmentPlannerRequestFromJson = (json: string): UploadConversationAttachmentPlannerRequest =>
  JSON.parse(json) as UploadConversationAttachmentPlannerRequest

export const uploadConversationAttachmentPlannerRequestToJson = (value: UploadConversationAttachmentPlannerRequest): string =>
  JSON.stringify(value)
