// 本文件定义 UnfavoriteBlogPostPlanner，负责博客域对应接口入口。

import type { BlogPostResponse } from '@/microservices/blog/objects/BlogPostResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const unfavoriteBlogPost = (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/UnfavoriteBlogPostPlanner', 'POST', { postId, userId: payload.userId })
