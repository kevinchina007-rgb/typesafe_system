export type BlogCommentResponse = {
  commentId: string
  postId: string
  authorUserId: string
  authorDisplayName: string
  authorAvatarUrl: string | null
  content: string
  createdAt: string
  isMyComment: boolean
  canDelete: boolean
}
