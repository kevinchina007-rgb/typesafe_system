// BlogProfilePlannerRequest：博客域博客主页请求对象。

export type BlogProfilePlannerRequest = {
  viewerUserId: string | null
  profileUserId: string
}

export const blogProfilePlannerRequestFromJson = (json: string): BlogProfilePlannerRequest =>
  JSON.parse(json) as BlogProfilePlannerRequest

export const blogProfilePlannerRequestToJson = (value: BlogProfilePlannerRequest): string =>
  JSON.stringify(value)