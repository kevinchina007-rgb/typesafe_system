// ModerateBlogPostPlannerRequest：博客域审核博客文章请求对象。

export type ModerateBlogPostPlannerRequest = {
  postId: string
}

export const moderateBlogPostPlannerRequestFromJson = (json: string): ModerateBlogPostPlannerRequest =>
  JSON.parse(json) as ModerateBlogPostPlannerRequest

export const moderateBlogPostPlannerRequestToJson = (value: ModerateBlogPostPlannerRequest): string =>
  JSON.stringify(value)