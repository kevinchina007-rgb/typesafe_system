// This file defines `tour-group` module `UpdateDirectConversationArchiveStatePlannerRequest` as request payload data and provides JSON helpers.

export type UpdateDirectConversationArchiveStatePlannerRequest = {
  archived: boolean
}
export const updateDirectConversationArchiveStatePlannerRequestFromJson = (json: string): UpdateDirectConversationArchiveStatePlannerRequest =>
  JSON.parse(json) as UpdateDirectConversationArchiveStatePlannerRequest

export const updateDirectConversationArchiveStatePlannerRequestToJson = (value: UpdateDirectConversationArchiveStatePlannerRequest): string =>
  JSON.stringify(value)
