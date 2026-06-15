// 本文件定义 ListBlogFollowingPlanner，负责博客域对应接口入口。

import type { BlogProfileUserListResponse } from '@/microservices/blog/objects/BlogProfileUserListResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const listBlogFollowing = (profileUserId: string, viewerUserId?: string): Promise<BlogProfileUserListResponse> =>
  executeJsonApiRequest('/ListBlogFollowingPlanner', 'POST', { profileUserId, viewerUserId })
