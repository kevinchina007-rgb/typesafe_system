// 本文件定义 content 模块的 `BlogCommentResponse`，作为响应数据并提供 JSON 编解码。

export type BlogCommentResponse = {
  commentId: string
  postId: string
  authorUserId: string
  authorDisplayName: string
  authorAvatarUrl: string | null
  content: string
  parentCommentId: string | null
  replyToUserId: string | null
  replyToDisplayName: string | null
  createdAt: string
  likeCount: number
  likedByCurrentUser: boolean
  isMyComment: boolean
  canDelete: boolean
}

export const blogCommentResponseFromJson = (json: string): BlogCommentResponse =>
  JSON.parse(json) as BlogCommentResponse

export const blogCommentResponseToJson = (value: BlogCommentResponse): string =>
  JSON.stringify(value)
