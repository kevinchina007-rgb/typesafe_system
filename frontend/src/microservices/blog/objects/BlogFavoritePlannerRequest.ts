// BlogFavoritePlannerRequest：博客域博客收藏请求对象。

export type BlogFavoritePlannerRequest = {
  postId: string
  userId: string
}

export const blogFavoritePlannerRequestFromJson = (json: string): BlogFavoritePlannerRequest =>
  JSON.parse(json) as BlogFavoritePlannerRequest

export const blogFavoritePlannerRequestToJson = (value: BlogFavoritePlannerRequest): string =>
  JSON.stringify(value)