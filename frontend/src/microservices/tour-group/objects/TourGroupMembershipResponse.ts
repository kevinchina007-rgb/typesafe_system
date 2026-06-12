// 本文件定义 tour-group 模块的 `TourGroupMembershipResponse`，作为响应数据并提供 JSON 编解码。

export type TourGroupMembershipResponse = {
  membershipId: string
  userId: string
  userDisplayName?: string
  status: string
  joinedAt: string
}
export const tourGroupMembershipResponseFromJson = (json: string): TourGroupMembershipResponse =>
  JSON.parse(json) as TourGroupMembershipResponse

export const tourGroupMembershipResponseToJson = (value: TourGroupMembershipResponse): string =>
  JSON.stringify(value)
