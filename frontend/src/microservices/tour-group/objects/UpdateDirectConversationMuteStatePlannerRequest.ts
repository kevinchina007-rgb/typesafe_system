// This file defines `tour-group` module `UpdateDirectConversationMuteStatePlannerRequest` as request payload data and provides JSON helpers.

export type UpdateDirectConversationMuteStatePlannerRequest = {
  muted: boolean
}
export const updateDirectConversationMuteStatePlannerRequestFromJson = (json: string): UpdateDirectConversationMuteStatePlannerRequest =>
  JSON.parse(json) as UpdateDirectConversationMuteStatePlannerRequest

export const updateDirectConversationMuteStatePlannerRequestToJson = (value: UpdateDirectConversationMuteStatePlannerRequest): string =>
  JSON.stringify(value)
