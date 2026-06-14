// This file defines `tour-group` module `SendTourGroupMessageAttachmentPlannerRequest` as request payload data and provides JSON helpers.

export type SendTourGroupMessageAttachmentPlannerRequest = {
  attachmentId: string
  attachmentType: string
  publicUrl: string
  originalFileName: string
  mimeType: string
  fileSize: number
  sortOrder: number
}
export const sendTourGroupMessageAttachmentPlannerRequestFromJson = (json: string): SendTourGroupMessageAttachmentPlannerRequest =>
  JSON.parse(json) as SendTourGroupMessageAttachmentPlannerRequest

export const sendTourGroupMessageAttachmentPlannerRequestToJson = (value: SendTourGroupMessageAttachmentPlannerRequest): string =>
  JSON.stringify(value)
