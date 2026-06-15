// 本文件定义 SaveBlogDraftPlanner，负责博客域对应接口入口。

import type { BlogPostResponse } from '@/microservices/blog/objects/BlogPostResponse'
import type { SaveBlogDraftPlannerRequest } from '@/microservices/blog/objects/SaveBlogDraftPlannerRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export type SaveBlogDraftPayload = SaveBlogDraftPlannerRequest

export const saveBlogDraft = (payload: SaveBlogDraftPayload): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/SaveBlogDraftPlanner', 'POST', payload)
