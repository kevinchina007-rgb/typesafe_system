// 本文件定义 tour-group 模块的 `TourGroupChatSettingsResponse`，作为响应数据并提供 JSON 编解码。

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
