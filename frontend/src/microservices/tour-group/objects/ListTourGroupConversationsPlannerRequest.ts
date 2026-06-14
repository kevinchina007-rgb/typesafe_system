// This file defines `tour-group` module `ListTourGroupConversationsPlannerRequest` as request payload data and provides JSON helpers.

export type ListTourGroupConversationsPlannerRequest = {
  groupId: string
  sessionId: string
}
export const listTourGroupConversationsPlannerRequestFromJson = (json: string): ListTourGroupConversationsPlannerRequest =>
  JSON.parse(json) as ListTourGroupConversationsPlannerRequest

export const listTourGroupConversationsPlannerRequestToJson = (value: ListTourGroupConversationsPlannerRequest): string =>
  JSON.stringify(value)
