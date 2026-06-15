// PublishBlogPostPlannerRequest：博客域发布博客文章请求对象。

export type PublishBlogPostPlannerRequest = {
  postId: string
  userId: string
}

export const publishBlogPostPlannerRequestFromJson = (json: string): PublishBlogPostPlannerRequest =>
  JSON.parse(json) as PublishBlogPostPlannerRequest

export const publishBlogPostPlannerRequestToJson = (value: PublishBlogPostPlannerRequest): string =>
  JSON.stringify(value)