// BlogLikePlannerRequest：博客域博客点赞请求对象。

export type BlogLikePlannerRequest = {
  postId: string
  userId: string
}

export const blogLikePlannerRequestFromJson = (json: string): BlogLikePlannerRequest =>
  JSON.parse(json) as BlogLikePlannerRequest

export const blogLikePlannerRequestToJson = (value: BlogLikePlannerRequest): string =>
  JSON.stringify(value)