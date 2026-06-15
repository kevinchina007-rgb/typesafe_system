// 本文件定义 traveler 模块的 `ListTravelersPlannerRequest`，作为列表请求参数并提供 JSON 编解码。

export type ListTravelersPlannerRequest = {
  actingUserId: string
  ownerUserId: string
}

export const listTravelersPlannerRequestFromJson = (json: string): ListTravelersPlannerRequest =>
  JSON.parse(json) as ListTravelersPlannerRequest

export const listTravelersPlannerRequestToJson = (value: ListTravelersPlannerRequest): string =>
  JSON.stringify(value)
