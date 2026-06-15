// BlogUserInteractionPlannerRequest：博客域博客用户Interaction请求对象。

export type BlogUserInteractionPlannerRequest = {
  userId: string
  targetUserId: string
}

export const blogUserInteractionPlannerRequestFromJson = (json: string): BlogUserInteractionPlannerRequest =>
  JSON.parse(json) as BlogUserInteractionPlannerRequest

export const blogUserInteractionPlannerRequestToJson = (value: BlogUserInteractionPlannerRequest): string =>
  JSON.stringify(value)