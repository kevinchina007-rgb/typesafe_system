// This file defines `tour-group` module `UpdateTourGroupChatSettingsPlannerInput` as request payload data and provides JSON helpers.

export type UpdateTourGroupChatSettingsPlannerInput = {
  groupId: string
  sessionId: string
  allowMemberDirectChat: boolean
}
export const updateTourGroupChatSettingsPlannerInputFromJson = (json: string): UpdateTourGroupChatSettingsPlannerInput =>
  JSON.parse(json) as UpdateTourGroupChatSettingsPlannerInput

export const updateTourGroupChatSettingsPlannerInputToJson = (value: UpdateTourGroupChatSettingsPlannerInput): string =>
  JSON.stringify(value)
