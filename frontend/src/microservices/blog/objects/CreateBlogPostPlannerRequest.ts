// CreateBlogPostPlannerRequest：博客域创建博客文章请求对象。

import type { ContentImagePlannerResponse } from '@/microservices/content/objects/ContentImagePlannerResponse'
import type { BlogTagResponse } from './BlogTagResponse'

export type CreateBlogPostPlannerRequest = {
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

export const createBlogPostPlannerRequestFromJson = (json: string): CreateBlogPostPlannerRequest =>
  JSON.parse(json) as CreateBlogPostPlannerRequest

export const createBlogPostPlannerRequestToJson = (value: CreateBlogPostPlannerRequest): string =>
  JSON.stringify(value)