// This file defines `tour-group` module `SendTourGroupChatMessagePlannerInput` as request payload data and provides JSON helpers.

import type { SendTourGroupMessagePlannerRequest } from './SendTourGroupMessagePlannerRequest'

export type SendTourGroupChatMessagePlannerInput = {
  groupId: string
  sessionId: string
  payload: SendTourGroupMessagePlannerRequest
}
export const sendTourGroupChatMessagePlannerInputFromJson = (json: string): SendTourGroupChatMessagePlannerInput =>
  JSON.parse(json) as SendTourGroupChatMessagePlannerInput

export const sendTourGroupChatMessagePlannerInputToJson = (value: SendTourGroupChatMessagePlannerInput): string =>
  JSON.stringify(value)
