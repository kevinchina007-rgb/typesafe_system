// This file defines `tour-group` module `SearchTourGroupConversationsPlannerRequest` as request payload data and provides JSON helpers.

export type SearchTourGroupConversationsPlannerRequest = {
  groupId: string
  sessionId: string
  query: string
}
export const searchTourGroupConversationsPlannerRequestFromJson = (json: string): SearchTourGroupConversationsPlannerRequest =>
  JSON.parse(json) as SearchTourGroupConversationsPlannerRequest

export const searchTourGroupConversationsPlannerRequestToJson = (value: SearchTourGroupConversationsPlannerRequest): string =>
  JSON.stringify(value)
