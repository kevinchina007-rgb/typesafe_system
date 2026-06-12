// 本文件定义 tour-group 模块的 `TourGroupBlacklistResponse`，作为列表响应数据并提供 JSON 编解码。

export type TourGroupBlacklistResponse = {
  blacklistId: string
  groupId: string
  userId: string
  blacklistedByUserId: string
  reason: string
  createdAt: string
}

export const tourGroupBlacklistResponseFromJson = (json: string): TourGroupBlacklistResponse =>
  JSON.parse(json) as TourGroupBlacklistResponse

export const tourGroupBlacklistResponseToJson = (value: TourGroupBlacklistResponse): string =>
  JSON.stringify(value)
