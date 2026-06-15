// 本文件定义 UnlikeBlogPostPlanner，负责博客域对应接口入口。

import type { BlogPostResponse } from '@/microservices/blog/objects/BlogPostResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const unlikeBlogPost = (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/UnlikeBlogPostPlanner', 'POST', { postId, userId: payload.userId })
