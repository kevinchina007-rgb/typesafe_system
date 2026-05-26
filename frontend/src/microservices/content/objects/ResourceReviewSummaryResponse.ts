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
