// 本文件定义 traveler 模块的 `TravelerDeletedPlannerResponse`，作为删除响应数据并提供 JSON 编解码。

export type TravelerDeletedPlannerResponse = {
  deleted: boolean
  hidden: boolean
}

export const travelerDeletedPlannerResponseFromJson = (json: string): TravelerDeletedPlannerResponse =>
  JSON.parse(json) as TravelerDeletedPlannerResponse

export const travelerDeletedPlannerResponseToJson = (value: TravelerDeletedPlannerResponse): string =>
  JSON.stringify(value)
