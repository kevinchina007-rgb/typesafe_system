// 本文件定义 BlockBlogUserPlanner，负责博客域对应接口入口。

import type { BlogProfileResponse } from '@/microservices/blog/objects/BlogProfileResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const blockBlogUser = (userId: string, targetUserId: string): Promise<BlogProfileResponse> =>
  executeJsonApiRequest('/BlockBlogUserPlanner', 'POST', { userId, targetUserId })
