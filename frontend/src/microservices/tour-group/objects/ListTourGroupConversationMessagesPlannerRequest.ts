// This file defines `tour-group` module `ListTourGroupConversationMessagesPlannerRequest` as request payload data and provides JSON helpers.

export type ListTourGroupConversationMessagesPlannerRequest = {
  conversationId: string
  sessionId: string
}
export const listTourGroupConversationMessagesPlannerRequestFromJson = (json: string): ListTourGroupConversationMessagesPlannerRequest =>
  JSON.parse(json) as ListTourGroupConversationMessagesPlannerRequest

export const listTourGroupConversationMessagesPlannerRequestToJson = (value: ListTourGroupConversationMessagesPlannerRequest): string =>
  JSON.stringify(value)
