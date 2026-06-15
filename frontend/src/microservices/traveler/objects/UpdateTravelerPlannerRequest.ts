// 本文件定义 traveler 模块的更新旅客请求，供前端直接使用。
import type { TravelerProfileInput } from './TravelerProfileInput'

export type UpdateTravelerPlannerRequest = TravelerProfileInput
export type UpdateTravelerRequest = UpdateTravelerPlannerRequest

export const updateTravelerPlannerRequestFromJson = (json: string): UpdateTravelerPlannerRequest =>
  JSON.parse(json) as UpdateTravelerPlannerRequest

export const updateTravelerPlannerRequestToJson = (value: UpdateTravelerPlannerRequest): string =>
  JSON.stringify(value)

export const updateTravelerRequestFromJson = updateTravelerPlannerRequestFromJson
export const updateTravelerRequestToJson = updateTravelerPlannerRequestToJson
