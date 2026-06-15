// 本文件定义 traveler 模块的 `DeleteTravelerPlannerRequest`，作为删除请求参数并提供 JSON 编解码。

export type DeleteTravelerPlannerRequest = {
  actingUserId: string
  ownerUserId: string
  travelerId: string
}

export const deleteTravelerPlannerRequestFromJson = (json: string): DeleteTravelerPlannerRequest =>
  JSON.parse(json) as DeleteTravelerPlannerRequest

export const deleteTravelerPlannerRequestToJson = (value: DeleteTravelerPlannerRequest): string =>
  JSON.stringify(value)
