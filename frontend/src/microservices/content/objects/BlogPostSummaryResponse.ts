import type { ContentImageResponse } from './ContentImageResponse'

export type BlogPostSummaryResponse = {
  postId: string
  authorUserId: string
  authorDisplayName: string
  authorAvatarUrl: string | null
  title: string
  summary: string
  status: string
  createdAt: string
  updatedAt: string
  publishedAt: string | null
  commentCount: number
  likeCount: number
  likedByCurrentUser: boolean
  isMyPost: boolean
  canEdit: boolean
  canArchive: boolean
  images: ContentImageResponse[]
  searchResultSnippet: string | null
}
export const blogPostSummaryResponseFromJson = (json: string): BlogPostSummaryResponse =>
  JSON.parse(json) as BlogPostSummaryResponse

export const blogPostSummaryResponseToJson = (value: BlogPostSummaryResponse): string =>
  JSON.stringify(value)
