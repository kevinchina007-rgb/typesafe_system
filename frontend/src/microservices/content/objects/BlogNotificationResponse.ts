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
