// This file defines `tour-group` module `UpdateTourGroupDirectConversationMuteStatePlannerInput` as request payload data and provides JSON helpers.

export type UpdateTourGroupDirectConversationMuteStatePlannerInput = {
  conversationId: string
  sessionId: string
  muted: boolean
}
export const updateTourGroupDirectConversationMuteStatePlannerInputFromJson = (json: string): UpdateTourGroupDirectConversationMuteStatePlannerInput =>
  JSON.parse(json) as UpdateTourGroupDirectConversationMuteStatePlannerInput

export const updateTourGroupDirectConversationMuteStatePlannerInputToJson = (value: UpdateTourGroupDirectConversationMuteStatePlannerInput): string =>
  JSON.stringify(value)
