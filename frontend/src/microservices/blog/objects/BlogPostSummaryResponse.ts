// 本文件定义 content 模块的 `BlogPostSummaryResponse`，作为摘要响应数据并提供 JSON 编解码。

import type { ContentImageResponse } from './ContentImageResponse'

export type BlogTagResponse = {
  tagType: string
  tagValue: string
}

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
  images: ContentImageResponse[]
  tags: BlogTagResponse[]
  searchResultSnippet: string | null
}

export const blogPostSummaryResponseFromJson = (json: string): BlogPostSummaryResponse =>
  JSON.parse(json) as BlogPostSummaryResponse

export const blogPostSummaryResponseToJson = (value: BlogPostSummaryResponse): string =>
  JSON.stringify(value)
