// CheckReviewEligibilityPlannerRequest：content 域评论资格判断请求对象。

export type CheckReviewEligibilityPlannerRequest = {
  userId: string
  orderItemId: string
}

export const checkReviewEligibilityPlannerRequestFromJson = (json: string): CheckReviewEligibilityPlannerRequest =>
  JSON.parse(json) as CheckReviewEligibilityPlannerRequest

export const checkReviewEligibilityPlannerRequestToJson = (value: CheckReviewEligibilityPlannerRequest): string =>
  JSON.stringify(value)
