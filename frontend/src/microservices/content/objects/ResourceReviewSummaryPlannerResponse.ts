// ResourceReviewSummaryPlannerResponse：content 域资源评论摘要返回对象。

export type ResourceReviewSummaryPlannerResponse = {
  resourceType: string
  resourceId: string
  averageRating: string
  reviewCount: number
}
export const resourceReviewSummaryPlannerResponseFromJson = (json: string): ResourceReviewSummaryPlannerResponse =>
  JSON.parse(json) as ResourceReviewSummaryPlannerResponse
export const resourceReviewSummaryPlannerResponseToJson = (value: ResourceReviewSummaryPlannerResponse): string =>
  JSON.stringify(value)
