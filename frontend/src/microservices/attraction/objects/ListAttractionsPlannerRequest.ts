// 本文件定义 attraction 模块的 `ListAttractionsPlannerRequest`，作为列表查询参数并提供 JSON 编解码。

export type ListAttractionsPlannerRequest = {
  city?: string
  keyword?: string
  useDate?: string
}

export const listAttractionsPlannerRequestFromJson = (json: string): ListAttractionsPlannerRequest =>
  JSON.parse(json) as ListAttractionsPlannerRequest

export const listAttractionsPlannerRequestToJson = (value: ListAttractionsPlannerRequest): string =>
  JSON.stringify(value)
