// This file defines `tour-group` module `UpdateTourGroupDirectConversationArchiveStatePlannerInput` as request payload data and provides JSON helpers.

export type UpdateTourGroupDirectConversationArchiveStatePlannerInput = {
  conversationId: string
  sessionId: string
  archived: boolean
}
export const updateTourGroupDirectConversationArchiveStatePlannerInputFromJson = (json: string): UpdateTourGroupDirectConversationArchiveStatePlannerInput =>
  JSON.parse(json) as UpdateTourGroupDirectConversationArchiveStatePlannerInput

export const updateTourGroupDirectConversationArchiveStatePlannerInputToJson = (value: UpdateTourGroupDirectConversationArchiveStatePlannerInput): string =>
  JSON.stringify(value)
