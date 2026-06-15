// ListMyReviewsPlannerRequest：content 域个人评论列表请求对象。

export type ListMyReviewsPlannerRequest = {
  userId: string
}

export const listMyReviewsPlannerRequestFromJson = (json: string): ListMyReviewsPlannerRequest =>
  JSON.parse(json) as ListMyReviewsPlannerRequest

export const listMyReviewsPlannerRequestToJson = (value: ListMyReviewsPlannerRequest): string =>
  JSON.stringify(value)
