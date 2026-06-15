// ReviewEligibilityPlannerResponse：content 域评论资格判断返回对象。

export type ReviewEligibilityPlannerResponse = {
  orderId: string
  orderItemId: string
  canReview: boolean
  alreadyReviewed: boolean
  reason: string | null
  resourceSummaryTitle: string
}
export const reviewEligibilityPlannerResponseFromJson = (json: string): ReviewEligibilityPlannerResponse =>
  JSON.parse(json) as ReviewEligibilityPlannerResponse
export const reviewEligibilityPlannerResponseToJson = (value: ReviewEligibilityPlannerResponse): string =>
  JSON.stringify(value)
