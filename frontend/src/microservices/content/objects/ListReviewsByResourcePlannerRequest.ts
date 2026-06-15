// ListReviewsByResourcePlannerRequest：content 域资源评论列表请求对象。

export type ListReviewsByResourcePlannerRequest = {
  userId: string
  resourceType: string
  resourceId: string
}

export const listReviewsByResourcePlannerRequestFromJson = (json: string): ListReviewsByResourcePlannerRequest =>
  JSON.parse(json) as ListReviewsByResourcePlannerRequest

export const listReviewsByResourcePlannerRequestToJson = (value: ListReviewsByResourcePlannerRequest): string =>
  JSON.stringify(value)
