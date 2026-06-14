// This file defines `tour-group` module `ListTourGroupDirectConversationsPlannerRequest` as request payload data and provides JSON helpers.

export type ListTourGroupDirectConversationsPlannerRequest = {
  groupId: string
  sessionId: string
}
export const listTourGroupDirectConversationsPlannerRequestFromJson = (json: string): ListTourGroupDirectConversationsPlannerRequest =>
  JSON.parse(json) as ListTourGroupDirectConversationsPlannerRequest

export const listTourGroupDirectConversationsPlannerRequestToJson = (value: ListTourGroupDirectConversationsPlannerRequest): string =>
  JSON.stringify(value)
