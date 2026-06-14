// 本文件定义 attraction 模块的 `BookAttractionItemPlannerRequest`，作为购票请求参数并提供 JSON 编解码。

export type BookAttractionItemPlannerRequest = {
  userId: string
  attractionId: string
  ticketTypeId: string
  sessionId?: string | null
  travelerIds: string[]
  useDate: string
}

export const bookAttractionItemPlannerRequestFromJson = (json: string): BookAttractionItemPlannerRequest =>
  JSON.parse(json) as BookAttractionItemPlannerRequest

export const bookAttractionItemPlannerRequestToJson = (value: BookAttractionItemPlannerRequest): string =>
  JSON.stringify(value)
