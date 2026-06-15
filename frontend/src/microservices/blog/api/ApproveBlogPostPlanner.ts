// 本文件定义 ApproveBlogPostPlanner，负责博客域对应接口入口。

import type { BlogPostResponse } from '@/microservices/blog/objects/BlogPostResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const approveBlogPost = (postId: string): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/ApproveBlogPostPlanner', 'POST', { postId })
