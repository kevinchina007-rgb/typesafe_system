// BlogCommentPlannerRequest：博客域博客评论请求对象。

export type BlogCommentPlannerRequest = {
  postId: string
  userId: string
  content: string
  parentCommentId?: string | null
  replyToUserId?: string | null
}

export const blogCommentPlannerRequestFromJson = (json: string): BlogCommentPlannerRequest =>
  JSON.parse(json) as BlogCommentPlannerRequest

export const blogCommentPlannerRequestToJson = (value: BlogCommentPlannerRequest): string =>
  JSON.stringify(value)