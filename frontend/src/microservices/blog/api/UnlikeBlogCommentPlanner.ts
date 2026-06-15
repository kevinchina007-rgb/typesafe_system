// 本文件定义 UnlikeBlogCommentPlanner，负责博客域对应接口入口。

import type { BlogPostResponse } from '@/microservices/blog/objects/BlogPostResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const unlikeBlogComment = (commentId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/UnlikeBlogCommentPlanner', 'POST', { commentId, userId: payload.userId })
