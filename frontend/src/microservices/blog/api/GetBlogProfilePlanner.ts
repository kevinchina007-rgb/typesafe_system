// 本文件定义 GetBlogProfilePlanner，负责博客域对应接口入口。

import type { BlogProfileResponse } from '@/microservices/blog/objects/BlogProfileResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const getBlogProfile = (profileUserId: string, viewerUserId?: string): Promise<BlogProfileResponse> =>
  executeJsonApiRequest('/GetBlogProfilePlanner', 'POST', { profileUserId, viewerUserId })
