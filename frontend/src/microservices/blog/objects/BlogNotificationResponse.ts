// 本文件定义 content 模块的 `BlogNotificationResponse`，作为响应数据并提供 JSON 编解码。

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

export type BlogNotificationListResponse = {
  notifications: BlogNotificationResponse[]
}
