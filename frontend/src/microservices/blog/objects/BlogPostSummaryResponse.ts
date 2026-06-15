// BlogPostSummaryResponse：博客域博客文章摘要返回对象。

import type { ContentImagePlannerResponse } from '@/microservices/content/objects/ContentImagePlannerResponse'
import type { BlogTagResponse } from './BlogTagResponse'

export type BlogPostSummaryResponse = {
  postId: string
  authorUserId: string
  authorDisplayName: string
  authorAvatarUrl: string | null
  title: string
  summary: string
  coverImageUrl: string | null
  coverText: string
  travelCity: string | null
  travelCities: string[]
  status: string
  createdAt: string
  updatedAt: string
  publishedAt: string | null
  commentCount: number
  likeCount: number
  favoriteCount: number
  likedByCurrentUser: boolean
  favoritedByCurrentUser: boolean
  isMyPost: boolean
  canEdit: boolean
  canArchive: boolean
  images: ContentImagePlannerResponse[]
  tags: BlogTagResponse[]
  searchResultSnippet: string | null
}

export const blogPostSummaryResponseFromJson = (json: string): BlogPostSummaryResponse =>
  JSON.parse(json) as BlogPostSummaryResponse

export const blogPostSummaryResponseToJson = (value: BlogPostSummaryResponse): string =>
  JSON.stringify(value)
