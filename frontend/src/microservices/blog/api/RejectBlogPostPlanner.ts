// 本文件定义 RejectBlogPostPlanner，负责博客域对应接口入口。

import type { BlogPostResponse } from '@/microservices/blog/objects/BlogPostResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const rejectBlogPost = (postId: string): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/RejectBlogPostPlanner', 'POST', { postId })
