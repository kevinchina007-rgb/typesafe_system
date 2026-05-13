import type { BlogCommentResponse } from './BlogCommentResponse'
import type { BlogPostSummaryResponse } from './BlogPostSummaryResponse'

export type BlogPostResponse = {
  post: BlogPostSummaryResponse
  content: string
  comments: BlogCommentResponse[]
}
