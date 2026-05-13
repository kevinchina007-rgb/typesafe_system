export type ReviewEligibilityResponse = {
  orderId: string
  orderItemId: string
  canReview: boolean
  alreadyReviewed: boolean
  reason: string | null
  resourceSummaryTitle: string
}
