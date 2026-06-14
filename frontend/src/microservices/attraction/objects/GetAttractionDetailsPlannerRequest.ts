// 本文件定义 attraction 模块的 `GetAttractionDetailsPlannerRequest`，作为详情查询参数并提供 JSON 编解码。

export type GetAttractionDetailsPlannerRequest = {
  attractionId: string
  useDate?: string
}

export const getAttractionDetailsPlannerRequestFromJson = (json: string): GetAttractionDetailsPlannerRequest =>
  JSON.parse(json) as GetAttractionDetailsPlannerRequest

export const getAttractionDetailsPlannerRequestToJson = (value: GetAttractionDetailsPlannerRequest): string =>
  JSON.stringify(value)
