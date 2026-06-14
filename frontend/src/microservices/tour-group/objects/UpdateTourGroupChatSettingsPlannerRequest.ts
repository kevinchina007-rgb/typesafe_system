// This file defines `tour-group` module `UpdateTourGroupChatSettingsPlannerRequest` as request payload data and provides JSON helpers.

export type UpdateTourGroupChatSettingsPlannerRequest = {
  allowMemberDirectChat: boolean
}
export const updateTourGroupChatSettingsPlannerRequestFromJson = (json: string): UpdateTourGroupChatSettingsPlannerRequest =>
  JSON.parse(json) as UpdateTourGroupChatSettingsPlannerRequest

export const updateTourGroupChatSettingsPlannerRequestToJson = (value: UpdateTourGroupChatSettingsPlannerRequest): string =>
  JSON.stringify(value)
