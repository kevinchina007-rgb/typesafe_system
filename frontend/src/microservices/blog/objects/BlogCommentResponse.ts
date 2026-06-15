// BlogCommentResponse：博客域博客评论返回对象。

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
