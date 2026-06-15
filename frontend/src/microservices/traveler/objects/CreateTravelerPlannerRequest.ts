// 本文件定义 traveler 模块的创建旅客请求，供前端直接使用。
import type { TravelerProfileInput } from './TravelerProfileInput'

export type CreateTravelerPlannerRequest = TravelerProfileInput
export type CreateTravelerRequest = CreateTravelerPlannerRequest

export const createTravelerPlannerRequestFromJson = (json: string): CreateTravelerPlannerRequest =>
  JSON.parse(json) as CreateTravelerPlannerRequest

export const createTravelerPlannerRequestToJson = (value: CreateTravelerPlannerRequest): string =>
  JSON.stringify(value)

export const createTravelerRequestFromJson = createTravelerPlannerRequestFromJson
export const createTravelerRequestToJson = createTravelerPlannerRequestToJson
