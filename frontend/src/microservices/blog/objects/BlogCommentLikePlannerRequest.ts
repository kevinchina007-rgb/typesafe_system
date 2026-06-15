// BlogCommentLikePlannerRequest：博客域博客评论点赞请求对象。

export type BlogCommentLikePlannerRequest = {
  commentId: string
  userId: string
}

export const blogCommentLikePlannerRequestFromJson = (json: string): BlogCommentLikePlannerRequest =>
  JSON.parse(json) as BlogCommentLikePlannerRequest

export const blogCommentLikePlannerRequestToJson = (value: BlogCommentLikePlannerRequest): string =>
  JSON.stringify(value)