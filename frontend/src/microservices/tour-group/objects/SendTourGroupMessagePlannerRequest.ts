// This file defines `tour-group` module `SendTourGroupMessagePlannerRequest` as request payload data and provides JSON helpers.

import type { SendTourGroupMessageAttachmentPlannerRequest } from './SendTourGroupMessageAttachmentPlannerRequest'

export type SendTourGroupMessagePlannerRequest = {
  messageType: string | null
  content: string
  replyToMessageId: string | null
  attachments: SendTourGroupMessageAttachmentPlannerRequest[]
}
export const sendTourGroupMessagePlannerRequestFromJson = (json: string): SendTourGroupMessagePlannerRequest =>
  JSON.parse(json) as SendTourGroupMessagePlannerRequest

export const sendTourGroupMessagePlannerRequestToJson = (value: SendTourGroupMessagePlannerRequest): string =>
  JSON.stringify(value)
