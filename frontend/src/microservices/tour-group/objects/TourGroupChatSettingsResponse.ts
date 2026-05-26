export type TourGroupChatSettingsResponse = {
  groupId: string
  allowMemberDirectChat: boolean
  updatedAt: string
  updatedByUserId: string
  canUpdate: boolean
}
export const tourGroupChatSettingsResponseFromJson = (json: string): TourGroupChatSettingsResponse =>
  JSON.parse(json) as TourGroupChatSettingsResponse

export const tourGroupChatSettingsResponseToJson = (value: TourGroupChatSettingsResponse): string =>
  JSON.stringify(value)
