// This file defines `tour-group` module `ListTourGroupChatMessagesPlannerRequest` as request payload data and provides JSON helpers.

export type ListTourGroupChatMessagesPlannerRequest = {
  groupId: string
  sessionId: string
}
export const listTourGroupChatMessagesPlannerRequestFromJson = (json: string): ListTourGroupChatMessagesPlannerRequest =>
  JSON.parse(json) as ListTourGroupChatMessagesPlannerRequest

export const listTourGroupChatMessagesPlannerRequestToJson = (value: ListTourGroupChatMessagesPlannerRequest): string =>
  JSON.stringify(value)
