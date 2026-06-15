// UpdateBlogProfilePrivacyPlannerRequest：博客域更新博客主页隐私请求对象。

export type UpdateBlogProfilePrivacyPlannerRequest = {
  userId: string
  hideRelations: boolean
}

export const updateBlogProfilePrivacyPlannerRequestFromJson = (json: string): UpdateBlogProfilePrivacyPlannerRequest =>
  JSON.parse(json) as UpdateBlogProfilePrivacyPlannerRequest

export const updateBlogProfilePrivacyPlannerRequestToJson = (value: UpdateBlogProfilePrivacyPlannerRequest): string =>
  JSON.stringify(value)