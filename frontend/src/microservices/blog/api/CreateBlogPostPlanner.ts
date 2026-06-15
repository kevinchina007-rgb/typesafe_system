// 本文件定义 CreateBlogPostPlanner，负责博客域对应接口入口。

import type { ContentImagePlannerResponse } from '@/microservices/content/objects/ContentImagePlannerResponse'
import type { BlogPostResponse } from '@/microservices/blog/objects/BlogPostResponse'
import type { BlogTagResponse } from '@/microservices/blog/objects/BlogTagResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const createBlogPost = (payload: {
  userId: string
  title: string
  summary: string
  content: string
  images: ContentImagePlannerResponse[]
  tags?: BlogTagResponse[]
  coverText?: string
  travelCity?: string | null
  travelCities?: string[] | null
}): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/CreateBlogPostPlanner', 'POST', payload)
