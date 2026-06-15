// 本文件定义 LikeBlogCommentPlanner，负责博客域对应接口入口。

import type { BlogPostResponse } from '@/microservices/blog/objects/BlogPostResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const likeBlogComment = (commentId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/LikeBlogCommentPlanner', 'POST', { commentId, userId: payload.userId })
