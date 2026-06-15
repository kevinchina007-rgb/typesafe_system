// BlogPostByIdPlannerRequest：博客域博客文章按id请求对象。

export type BlogPostByIdPlannerRequest = {
  postId: string
  userId?: string | null
}

export const blogPostByIdPlannerRequestFromJson = (json: string): BlogPostByIdPlannerRequest =>
  JSON.parse(json) as BlogPostByIdPlannerRequest

export const blogPostByIdPlannerRequestToJson = (value: BlogPostByIdPlannerRequest): string =>
  JSON.stringify(value)