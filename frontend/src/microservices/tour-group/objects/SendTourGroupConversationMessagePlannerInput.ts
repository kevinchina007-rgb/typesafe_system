// This file defines `tour-group` module `SendTourGroupConversationMessagePlannerInput` as request payload data and provides JSON helpers.

import type { SendTourGroupMessagePlannerRequest } from './SendTourGroupMessagePlannerRequest'

export type SendTourGroupConversationMessagePlannerInput = {
  conversationId: string
  sessionId: string
  payload: SendTourGroupMessagePlannerRequest
}
export const sendTourGroupConversationMessagePlannerInputFromJson = (json: string): SendTourGroupConversationMessagePlannerInput =>
  JSON.parse(json) as SendTourGroupConversationMessagePlannerInput

export const sendTourGroupConversationMessagePlannerInputToJson = (value: SendTourGroupConversationMessagePlannerInput): string =>
  JSON.stringify(value)
