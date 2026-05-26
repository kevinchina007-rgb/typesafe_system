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
