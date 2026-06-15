// GetReviewSummaryPlannerRequest：content 域资源评论摘要请求对象。

export type GetReviewSummaryPlannerRequest = {
  userId: string
  resourceType: string
  resourceId: string
}

export const getReviewSummaryPlannerRequestFromJson = (json: string): GetReviewSummaryPlannerRequest =>
  JSON.parse(json) as GetReviewSummaryPlannerRequest

export const getReviewSummaryPlannerRequestToJson = (value: GetReviewSummaryPlannerRequest): string =>
  JSON.stringify(value)
