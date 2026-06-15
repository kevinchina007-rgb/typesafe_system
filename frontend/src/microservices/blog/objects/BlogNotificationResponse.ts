// BlogNotificationResponse：博客域博客通知返回对象。

export type BlogNotificationResponse = {
  notificationId: string
  receiverUserId: string
  actorUserId: string | null
  actorDisplayName: string | null
  actorAvatarUrl: string | null
  notificationType: string
  postId: string | null
  commentId: string | null
  content: string
  isRead: boolean
  createdAt: string
}
export const blogNotificationResponseFromJson = (json: string): BlogNotificationResponse =>
  JSON.parse(json) as BlogNotificationResponse

export const blogNotificationResponseToJson = (value: BlogNotificationResponse): string =>
  JSON.stringify(value)