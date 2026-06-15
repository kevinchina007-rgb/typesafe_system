// DeleteReviewPlannerRequest：content 域删除评论请求对象。

export type DeleteReviewPlannerRequest = {
  userId: string
  reviewId: string
}

export const deleteReviewPlannerRequestFromJson = (json: string): DeleteReviewPlannerRequest =>
  JSON.parse(json) as DeleteReviewPlannerRequest

export const deleteReviewPlannerRequestToJson = (value: DeleteReviewPlannerRequest): string =>
  JSON.stringify(value)
