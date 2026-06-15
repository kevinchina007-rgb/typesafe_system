// BlogNotificationListResponse：博客域博客通知列表返回对象。

import type { BlogNotificationResponse } from './BlogNotificationResponse'

export type BlogNotificationListResponse = {
  notifications: BlogNotificationResponse[]
}

export const blogNotificationListResponseFromJson = (json: string): BlogNotificationListResponse =>
  JSON.parse(json) as BlogNotificationListResponse

export const blogNotificationListResponseToJson = (value: BlogNotificationListResponse): string =>
  JSON.stringify(value)