// SaveBlogDraftPlannerRequest：博客域保存博客草稿请求对象。

import type { ContentImagePlannerResponse } from '@/microservices/content/objects/ContentImagePlannerResponse'
import type { BlogTagResponse } from './BlogTagResponse'

export type SaveBlogDraftPlannerRequest = {
  userId: string
  postId?: string | null
  title: string
  summary: string
  coverText: string
  content: string
  images: ContentImagePlannerResponse[]
  tags: BlogTagResponse[]
  travelCity?: string | null
  travelCities?: string[] | null
}

export const saveBlogDraftPlannerRequestFromJson = (json: string): SaveBlogDraftPlannerRequest =>
  JSON.parse(json) as SaveBlogDraftPlannerRequest

export const saveBlogDraftPlannerRequestToJson = (value: SaveBlogDraftPlannerRequest): string =>
  JSON.stringify(value)