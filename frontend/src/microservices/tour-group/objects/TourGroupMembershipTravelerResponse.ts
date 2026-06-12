// 本文件定义 tour-group 模块的 `TourGroupMembershipTravelerResponse`，作为响应数据并提供 JSON 编解码。

export type TourGroupMembershipTravelerResponse = {
  membershipTravelerId: string
  membershipId: string
  travelerId: string
  status: string
  joinedAt: string
}
export const tourGroupMembershipTravelerResponseFromJson = (json: string): TourGroupMembershipTravelerResponse =>
  JSON.parse(json) as TourGroupMembershipTravelerResponse

export const tourGroupMembershipTravelerResponseToJson = (value: TourGroupMembershipTravelerResponse): string =>
  JSON.stringify(value)
