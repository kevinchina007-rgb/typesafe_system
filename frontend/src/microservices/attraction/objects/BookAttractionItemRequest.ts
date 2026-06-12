// 本文件定义 attraction 模块的 `BookAttractionItemRequest`，作为请求参数并提供 JSON 编解码。

export type BookAttractionItemRequest = {
  userId: string
  attractionId: string
  ticketTypeId: string
  sessionId?: string | null
  travelerIds: string[]
  useDate: string
}

export const bookAttractionItemRequestFromJson = (json: string): BookAttractionItemRequest =>
  JSON.parse(json) as BookAttractionItemRequest

export const bookAttractionItemRequestToJson = (value: BookAttractionItemRequest): string =>
  JSON.stringify(value)
