// ListBlogPostsPlannerRequest：博客域博客文章列表请求对象。

export type ListBlogPostsPlannerRequest = {
  scope?: 'home' | 'latest' | 'mine' | 'profile' | 'drafts' | 'favorites' | 'pending' | 'reviewed'
  userId?: string
  q?: string
  tagType?: string
  tagValue?: string
  travelCity?: string
  travelCities?: string[]
}

export const listBlogPostsPlannerRequestFromJson = (json: string): ListBlogPostsPlannerRequest =>
  JSON.parse(json) as ListBlogPostsPlannerRequest

export const listBlogPostsPlannerRequestToJson = (value: ListBlogPostsPlannerRequest): string =>
  JSON.stringify(value)