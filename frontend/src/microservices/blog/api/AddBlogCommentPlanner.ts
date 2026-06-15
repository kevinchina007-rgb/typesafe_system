// 本文件定义 AddBlogCommentPlanner，负责博客域对应接口入口。

import type { BlogPostResponse } from '@/microservices/blog/objects/BlogPostResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const addBlogComment = (
  postId: string,
  payload: { userId: string; content: string; parentCommentId?: string | null; replyToUserId?: string | null },
): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/AddBlogCommentPlanner', 'POST', { postId, ...payload })
