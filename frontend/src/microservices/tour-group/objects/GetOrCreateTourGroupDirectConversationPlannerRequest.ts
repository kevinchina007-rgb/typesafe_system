// This file defines `tour-group` module `GetOrCreateTourGroupDirectConversationPlannerRequest` as request payload data and provides JSON helpers.

export type GetOrCreateTourGroupDirectConversationPlannerRequest = {
  targetUserId: string
}
export const getOrCreateTourGroupDirectConversationPlannerRequestFromJson = (json: string): GetOrCreateTourGroupDirectConversationPlannerRequest =>
  JSON.parse(json) as GetOrCreateTourGroupDirectConversationPlannerRequest

export const getOrCreateTourGroupDirectConversationPlannerRequestToJson = (value: GetOrCreateTourGroupDirectConversationPlannerRequest): string =>
  JSON.stringify(value)
