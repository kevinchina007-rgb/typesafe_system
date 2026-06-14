// This file defines `tour-group` module `LoadTourGroupChatSettingsPlannerRequest` as request payload data and provides JSON helpers.

export type LoadTourGroupChatSettingsPlannerRequest = {
  groupId: string
  sessionId: string
}
export const loadTourGroupChatSettingsPlannerRequestFromJson = (json: string): LoadTourGroupChatSettingsPlannerRequest =>
  JSON.parse(json) as LoadTourGroupChatSettingsPlannerRequest

export const loadTourGroupChatSettingsPlannerRequestToJson = (value: LoadTourGroupChatSettingsPlannerRequest): string =>
  JSON.stringify(value)
