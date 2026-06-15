// 本文件定义 GetBlogPostPlanner，负责博客域对应接口入口。

import type { BlogPostResponse } from '@/microservices/blog/objects/BlogPostResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const getBlogPost = (postId: string, userId?: string): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/GetBlogPostPlanner', 'POST', { postId, userId })
