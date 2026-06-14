// This file defines `tour-group` module `GetOrCreateTourGroupDirectConversationPlannerInput` as request payload data and provides JSON helpers.

export type GetOrCreateTourGroupDirectConversationPlannerInput = {
  groupId: string
  sessionId: string
  targetUserId: string
}
export const getOrCreateTourGroupDirectConversationPlannerInputFromJson = (json: string): GetOrCreateTourGroupDirectConversationPlannerInput =>
  JSON.parse(json) as GetOrCreateTourGroupDirectConversationPlannerInput

export const getOrCreateTourGroupDirectConversationPlannerInputToJson = (value: GetOrCreateTourGroupDirectConversationPlannerInput): string =>
  JSON.stringify(value)
