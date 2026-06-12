// 本文件定义 content 模块的 `ReviewEligibilityResponse`，作为响应数据并提供 JSON 编解码。

export type ReviewEligibilityResponse = {
  orderId: string
  orderItemId: string
  canReview: boolean
  alreadyReviewed: boolean
  reason: string | null
  resourceSummaryTitle: string
}
export const reviewEligibilityResponseFromJson = (json: string): ReviewEligibilityResponse =>
  JSON.parse(json) as ReviewEligibilityResponse

export const reviewEligibilityResponseToJson = (value: ReviewEligibilityResponse): string =>
  JSON.stringify(value)
