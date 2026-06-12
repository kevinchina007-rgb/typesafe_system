// 本文件定义 content 模块的 `ResourceReviewSummaryResponse`，作为摘要响应数据并提供 JSON 编解码。

export type ResourceReviewSummaryResponse = {
  resourceType: string
  resourceId: string
  averageRating: string
  reviewCount: number
}
export const resourceReviewSummaryResponseFromJson = (json: string): ResourceReviewSummaryResponse =>
  JSON.parse(json) as ResourceReviewSummaryResponse

export const resourceReviewSummaryResponseToJson = (value: ResourceReviewSummaryResponse): string =>
  JSON.stringify(value)
