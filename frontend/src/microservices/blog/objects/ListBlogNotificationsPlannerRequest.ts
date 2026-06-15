// ListBlogNotificationsPlannerRequest：博客域博客通知列表请求对象。

export type ListBlogNotificationsPlannerRequest = {
  userId: string
}

export const listBlogNotificationsPlannerRequestFromJson = (json: string): ListBlogNotificationsPlannerRequest =>
  JSON.parse(json) as ListBlogNotificationsPlannerRequest

export const listBlogNotificationsPlannerRequestToJson = (value: ListBlogNotificationsPlannerRequest): string =>
  JSON.stringify(value)