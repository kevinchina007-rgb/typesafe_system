// 本文件定义 FavoriteBlogPostPlanner，负责博客域对应接口入口。

import type { BlogPostResponse } from '@/microservices/blog/objects/BlogPostResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const favoriteBlogPost = (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/FavoriteBlogPostPlanner', 'POST', { postId, userId: payload.userId })
