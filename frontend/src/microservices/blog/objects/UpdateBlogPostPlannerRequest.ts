// UpdateBlogPostPlannerRequest：博客域更新博客文章请求对象。

import type { ContentImagePlannerResponse } from '@/microservices/content/objects/ContentImagePlannerResponse'
import type { BlogTagResponse } from './BlogTagResponse'

export type UpdateBlogPostPlannerRequest = {
  postId: string
  userId: string
  title: string
  summary: string
  content: string
  images: ContentImagePlannerResponse[]
  tags?: BlogTagResponse[]
  coverText?: string
  travelCity?: string | null
  travelCities?: string[] | null
}

export const updateBlogPostPlannerRequestFromJson = (json: string): UpdateBlogPostPlannerRequest =>
  JSON.parse(json) as UpdateBlogPostPlannerRequest

export const updateBlogPostPlannerRequestToJson = (value: UpdateBlogPostPlannerRequest): string =>
  JSON.stringify(value)