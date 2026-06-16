// 本文件定义 CreateAttractionPlanner 的景点创建请求，供管理端创建景点时使用。

export type CreateAttractionPlannerRequest = {
  managerId: string
  attractionName: string
  city: string
  location: string
  description: string
  imageUrl?: string | null
}

export const createAttractionPlannerRequestFromJson = (json: string): CreateAttractionPlannerRequest =>
  JSON.parse(json) as CreateAttractionPlannerRequest

export const createAttractionPlannerRequestToJson = (value: CreateAttractionPlannerRequest): string =>
  JSON.stringify(value)
