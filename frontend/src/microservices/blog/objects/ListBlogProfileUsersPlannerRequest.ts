// ListBlogProfileUsersPlannerRequest：博客域博客主页用户列表请求对象。

export type ListBlogProfileUsersPlannerRequest = {
  profileUserId: string
  viewerUserId: string | null
}

export const listBlogProfileUsersPlannerRequestFromJson = (json: string): ListBlogProfileUsersPlannerRequest =>
  JSON.parse(json) as ListBlogProfileUsersPlannerRequest

export const listBlogProfileUsersPlannerRequestToJson = (value: ListBlogProfileUsersPlannerRequest): string =>
  JSON.stringify(value)